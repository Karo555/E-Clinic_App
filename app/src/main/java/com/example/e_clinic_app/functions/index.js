/**
 * 1. Import required modules
 */
const functions = require('firebase-functions');
const admin = require('firebase-admin');

/**
 * 2. Initialize the Admin SDK
 *    This automatically picks up the service account when deployed to Firebase.
 */
admin.initializeApp();
const db = admin.firestore();

/**
 * 3. onNewChatMessage Cloud Function
 *
 *    - Trigger: When a new document is created under "chats/{chatId}/messages/{messageId}"
 *    - Action:
 *        a) Read the newly written message (senderId, text, timestamp).
 *        b) Read the parent chat doc to get doctorId & patientId.
 *        c) Determine recipientId (the opposite of senderId).
 *        d) Look up that user’s fcmToken in /users/{recipientId}.fcmToken.
 *        e) If fcmToken exists, send a push notification via FCM.
 */
exports.onNewChatMessage = functions.firestore
  .document('chats/{chatId}/messages/{messageId}')
  .onCreate(async (snap, context) => {
    // 3.1: Fetch the message data
    const messageData = snap.data();
    if (!messageData) {
      console.log('No data in new chat message snapshot.');
      return null;
    }
    const { senderId, text } = messageData;

    // 3.2: Extract chatId from the context, then read the parent chat document
    const chatId = context.params.chatId; // e.g. "doctor123_patient456"
    try {
      const chatDocRef = db.collection('chats').doc(chatId);
      const chatDocSnap = await chatDocRef.get();
      if (!chatDocSnap.exists) {
        console.log(`Chat document ${chatId} does not exist.`);
        return null;
      }
      const chatFields = chatDocSnap.data();
      const doctorId = chatFields.doctorId;
      const patientId = chatFields.patientId;

      // 3.3: Determine recipient: if sender is patient, send to doctor, otherwise send to patient
      let recipientId;
      if (senderId === patientId) {
        recipientId = doctorId;
      } else if (senderId === doctorId) {
        recipientId = patientId;
      } else {
        console.log(
          `SenderId (${senderId}) is neither patientId (${patientId}) nor doctorId (${doctorId}). Aborting.`
        );
        return null;
      }

      // 3.4: Load recipient’s user document to get fcmToken
      const userDocRef = db.collection('users').doc(recipientId);
      const userDocSnap = await userDocRef.get();
      if (!userDocSnap.exists) {
        console.log(`User document for recipientId=${recipientId} not found.`);
        return null;
      }
      const fcmToken = userDocSnap.get('fcmToken');
      if (!fcmToken) {
        console.log(`No fcmToken for user ${recipientId}.`);
        return null;
      }

      // 3.5: Build notification payload
      const truncatedBody = text.length > 100 ? text.substring(0, 100) + '…' : text;
      const payload = {
        notification: {
          title: 'New Message',
          body: truncatedBody,
        },
        data: {
          chatId: chatId,
          senderId: senderId,
          click_action: 'FLUTTER_NOTIFICATION_CLICK', // or your own action if needed
        },
      };

      // 3.6: Send a push to the device token
      const response = await admin.messaging().sendToDevice(fcmToken, payload);
      console.log(
        `Notification sent to user ${recipientId} (token=${fcmToken}). Response:`,
        response
      );

      return null;
    } catch (error) {
      console.error('Error in onNewChatMessage function:', error);
      return null;
    }
  });