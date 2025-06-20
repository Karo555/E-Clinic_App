import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
admin.initializeApp();
// functions/src/index.ts

export const createInstitutionAdmin = functions.https.onCall(async (data, context) => {
    // Check if request is authenticated
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'Only authenticated users can create admins.');
    }

    // Optionally check if current user is a Global Admin
    const requesterUID = context.auth.uid;
    const requesterDoc = await admin.firestore().collection('users').doc(requesterUID).get();

    if (!requesterDoc.exists || requesterDoc.data()?.role !== 'ADMIN') {
        throw new functions.https.HttpsError('permission-denied', 'Only Global Admins can create institution admins.');
    }

    // Get data from client
    const { email, password, institutionId } = data;

    if (!email || !password || !institutionId) {
        throw new functions.https.HttpsError('invalid-argument', 'Email, password, and institutionId are required.');
    }

    // Create the new user
    const userRecord = await admin.auth().createUser({
        email: email,
        password: password,
        emailVerified: false,
        disabled: false,
    });

    // Add to Firestore
    await admin.firestore().collection('users').doc(userRecord.uid).set({
        email: email,
        institutionId: institutionId,
        role: 'INSTITUTION_ADMIN',
        uid: userRecord.uid,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    return { message: 'Institution admin created successfully.' };
});

/**
 * functions/index.js
 *
 * Uses the Firebase Functions v1 API:
 *  1) onNewChatMessage — triggers on new documents under /chats/{chatId}/messages/{messageId}
 *  2) dailyAppointmentReminder — runs once per day at midnight UTC
 */

const { firestore, pubsub } = require("firebase-functions/v1");
const admin = require("firebase-admin");

// Firestore reference
const db = admin.firestore();

/**
 * 1) onNewChatMessage
 *
 * Trigger on any new document created under:
 *   /chats/{chatId}/messages/{messageId}
 *
 * Expects each `chats/{chatId}` to have fields: { doctorId, patientId }.
 * When a message arrives, we determine the “other” participant (recipient),
 * fetch their fcmToken from /users/{recipientId}/fcmToken, and send a push.
 */
exports.onNewChatMessage = functions.firestore
  .document("chats/{chatId}/messages/{messageId}")
  .onCreate(async (snap, context) => {
    try {
      // 1. Read the new message data
      const messageData = snap.data();
      if (!messageData) {
        console.log("onNewChatMessage: no message data found");
        return null;
      }
      const { senderId, text = "" } = messageData;

      // 2. Read the parent chat document
      const chatId = context.params.chatId; // e.g. "doctor123_patient456"
      const chatDocRef = db.collection("chats").doc(chatId);
      const chatSnap = await chatDocRef.get();
      if (!chatSnap.exists) {
        console.log(`onNewChatMessage: chat ${chatId} does not exist`);
        return null;
      }
    const chatData = chatSnap.data();
    if (!chatData || !chatData.doctorId || !chatData.patientId) {
      console.log(`onNewChatMessage: invalid chat data for chatId=${chatId}`);
      return null;
    }
    const { doctorId, patientId } = chatData;
      // 3. Determine the recipient (the opposite of senderId)
      let recipientId;
      if (senderId === patientId) {
        recipientId = doctorId;
      } else if (senderId === doctorId) {
        recipientId = patientId;
      } else {
        console.log(
          `onNewChatMessage: senderId (${senderId}) is neither doctorId (${doctorId}) nor patientId (${patientId})`
        );
        return null;
      }

      // 4. Fetch the recipient’s FCM token
      const userSnap = await db.collection("users").doc(recipientId).get();
      if (!userSnap.exists) {
        console.log(`onNewChatMessage: no user doc for recipientId=${recipientId}`);
        return null;
      }
      const fcmToken = userSnap.get("fcmToken");
      if (!fcmToken) {
        console.log(`onNewChatMessage: no fcmToken for user ${recipientId}`);
        return null;
      }

      // 5. Build and send the notification payload
      const truncatedText = text.length > 100 ? text.substring(0, 100) + "…" : text;
      const payload = {
        notification: {
          title: "New Message",
          body: truncatedText,
        },
        data: {
          chatId,
          senderId,
          click_action: "FLUTTER_NOTIFICATION_CLICK",
        },
      };

      const response = await admin.messaging().sendToDevice(fcmToken, payload);
      console.log(
        `onNewChatMessage: notification sent to ${recipientId} (token=${fcmToken}), response=`,
        response
      );
      return null;
    } catch (error) {
      console.error("onNewChatMessage: error sending notification", error);
      return null;
    }
  });

/**
 * 2) dailyAppointmentReminder
 *
 * Runs every day at 00:00 UTC. Queries `/appointments` for any appointment
 * scheduled ~24 hours from now (status == "scheduled"), then pushes a reminder
 * to each patient’s device via their fcmToken under /users/{patientId}/fcmToken.
 */
exports.dailyAppointmentReminder = functions.pubsub
  .schedule("0 0 * * *") // Every day at midnight UTC
  .timeZone("UTC")
  .onRun(async (context) => {
    try {
      const now = admin.firestore.Timestamp.now();
      // 24 hours from now
      const in24h = admin.firestore.Timestamp.fromDate(
        new Date(now.toDate().getTime() + 24 * 60 * 60 * 1000)
      );
      // 25 hours from now (window end)
      const in25h = admin.firestore.Timestamp.fromDate(
        new Date(in24h.toDate().getTime() + 60 * 60 * 1000)
      );

      // 1) Query appointments with status="scheduled" and date in [in24h, in25h)
      const appointmentsSnap = await db
        .collection("appointments")
        .where("status", "==", "scheduled")
        .where("date", ">=", in24h)
        .where("date", "<", in25h)
        .get();

      if (appointmentsSnap.empty) {
        console.log("dailyAppointmentReminder: no appointments 24h out");
        return null;
      }

      // 2) For each matching appointment, send a reminder to the patient
      const sendPromises = [];
      appointmentsSnap.forEach((docSnap) => {
        const data = docSnap.data();
        const {
          date: appointmentTimestamp,
          doctorFirstName,
          doctorLastName,
          patientFirstName,
          patientLastName,
          patientId,
          doctorId,
        } = data;

        // Format the appointment date/time
        const appointmentDateStr = appointmentTimestamp
          .toDate()
          .toLocaleString("en-US", {
            month: "short",
            day: "numeric",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
          });

        // Fetch patient’s FCM token
        const p = db
          .collection("users")
          .doc(patientId)
          .get()
          .then((userSnap) => {
            if (!userSnap.exists) {
              console.log(`dailyAppointmentReminder: no user for patientId=${patientId}`);
              return null;
            }
            const fcmToken = userSnap.get("fcmToken");
            if (!fcmToken) {
              console.log(`dailyAppointmentReminder: no fcmToken for patient ${patientId}`);
              return null;
            }

            // Build notification payload
            const payload = {
              notification: {
                title: "Appointment Reminder",
                body: `Hi ${patientFirstName}, you have an appointment with Dr. ${doctorFirstName} ${doctorLastName} on ${appointmentDateStr}.`,
              },
              data: {
                appointmentId: docSnap.id,
                doctorId,
                click_action: "FLUTTER_NOTIFICATION_CLICK",
              },
            };

            return admin.messaging().sendToDevice(fcmToken, payload);
          })
          .then((response) => {
            if (response && response.results) {
              const result0 = response.results[0];
              if (result0.error) {
                console.error(
                  `dailyAppointmentReminder: push failed for patientId=${patientId}`,
                  result0.error
                );
              } else {
                console.log(
                  `dailyAppointmentReminder: reminder sent to patientId=${patientId} for appointmentId=${docSnap.id}`
                );
              }
            }
            return null;
          })
          .catch((err) => {
            console.error(
              `dailyAppointmentReminder: error sending push for patient ${patientId}`,
              err
            );
            return null;
          });

        sendPromises.push(p);
      });

      // Wait for all sends to complete
      await Promise.all(sendPromises);
      console.log("dailyAppointmentReminder: all reminders processed");
      return null;
    } catch (error) {
      console.error("dailyAppointmentReminder: error", error);
      return null;
    }
  });