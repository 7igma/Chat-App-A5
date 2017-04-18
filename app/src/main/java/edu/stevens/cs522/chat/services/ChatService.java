package edu.stevens.cs522.chat.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.common.net.InetAddresses;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import edu.stevens.cs522.chat.activities.SettingsActivity;
import edu.stevens.cs522.chat.async.IContinue;
import edu.stevens.cs522.chat.contracts.PeerContract;
import edu.stevens.cs522.chat.entities.ChatMessage;
import edu.stevens.cs522.chat.entities.Peer;
import edu.stevens.cs522.chat.managers.MessageManager;
import edu.stevens.cs522.chat.managers.PeerManager;
import edu.stevens.cs522.chat.util.InetAddressUtils;

import static android.app.Activity.RESULT_OK;


public class ChatService extends Service implements IChatService, SharedPreferences.OnSharedPreferenceChangeListener {

    protected static final String TAG = ChatService.class.getCanonicalName();

    protected static final String SEND_TAG = "ChatSendThread";

    protected static final String RECEIVE_TAG = "ChatReceiveThread";

    protected IBinder binder = new ChatBinder();

    protected SendHandler sendHandler;

    protected Thread receiveThread;

    protected Thread sendThread;
    protected Thread sendThread2;

    protected DatagramSocket chatSocket;

    protected boolean socketOK = true;

    protected boolean finished = false;

    PeerManager peerManager;

    MessageManager messageManager;

    protected int chatPort;

    @Override
    public void onCreate() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //chatPort = prefs.getInt(SettingsActivity.APP_PORT_KEY, 6666);
        String chatPortStr = prefs.getString(SettingsActivity.APP_PORT_KEY, "6666");
        chatPort = Integer.parseInt(chatPortStr);
        prefs.registerOnSharedPreferenceChangeListener(this);

        peerManager = new PeerManager(this);
        messageManager = new MessageManager(this);

        try {
            chatSocket = new DatagramSocket(chatPort);
        } catch (Exception e) {
            IllegalStateException ex = new IllegalStateException("Unable to init client socket.");
            ex.initCause(e);
            throw ex;
        }

        // TODO initialize the thread that sends messages
        Log.i(TAG, "DEBUG: INITIALIZING THE HANDLER THREAD");
        /*
        HandlerThread thread = new HandlerThread(SEND_TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        if (thread.getLooper() == Looper.getMainLooper())
        {
            Log.i(TAG, "DEBUG: LOOPER IS MAIN LOOPER");
        }
        else
        {
            Log.i(TAG, "DEBUG: LOOPER IS DIFFERENT THREAD");
        }
        sendHandler = new SendHandler(thread.getLooper());
        */
        sendThread = new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                if (Looper.myLooper() == Looper.getMainLooper())
                {
                    Log.i(TAG, "DEBUG: Creation: LOOPER IS MAIN LOOPER");
                }
                else
                {
                    Log.i(TAG, "DEBUG: Creation: LOOPER IS NOT THE MAIN LOOPER");
                }
                sendHandler = new SendHandler(Looper.myLooper());

                Looper.loop();
            }
        });
        sendThread.start();

        // end TODO

        receiveThread = new Thread(new ReceiverThread());
        receiveThread.start();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service is being killed");
        finished = true;
        sendHandler.getLooper().getThread().interrupt();  // No-op?
        sendHandler.getLooper().quit();
        receiveThread.interrupt();
        chatSocket.close();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public final class ChatBinder extends Binder {

        public IChatService getService() {
            return ChatService.this;
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(SettingsActivity.APP_PORT_KEY)) {
            try {
                chatSocket.close();
                chatPort = prefs.getInt(SettingsActivity.APP_PORT_KEY, SettingsActivity.DEFAULT_APP_PORT);
                chatSocket = new DatagramSocket(chatPort);
            } catch (IOException e) {
                IllegalStateException ex = new IllegalStateException("Unable to change client socket.");
                ex.initCause(e);
                throw ex;
            }
        }
    }

    @Override
    public void send(InetAddress destAddress, int destPort, String sender, String messageText, ResultReceiver receiver) {
        final Message message = sendHandler.obtainMessage();
        // TODO send the message to the sending thread
        Bundle args = new Bundle();
        args.putSerializable("address", destAddress);
        args.putParcelable("receiver", receiver);
        args.putString("sender", sender);
        args.putInt("port", destPort);
        args.putString("msg", messageText);
        message.setData(args);
        sendThread2 = new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                sendHandler.handleMessage(message);
                Looper.loop();
            }
        });
        sendThread2.start();
        //sendHandler.handleMessage(message);


    }


    private final class SendHandler extends Handler {

        public static final String CHAT_NAME = "edu.stevens.cs522.chat.services.extra.CHAT_NAME";
        public static final String CHAT_MESSAGE = "edu.stevens.cs522.chat.services.extra.CHAT_MESSAGE";
        public static final String DEST_ADDRESS = "edu.stevens.cs522.chat.services.extra.DEST_ADDRESS";
        public static final String DEST_PORT = "edu.stevens.cs522.chat.services.extra.DEST_PORT";
        public static final String RECEIVER = "edu.stevens.cs522.chat.services.extra.RECEIVER";

        public SendHandler() {
            super();
        }

        public SendHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {

            if (sendThread.isAlive())
            {
                Log.i(TAG, "HANDLE MESSAGE: SEND THREAD IS STILL ALIVE");
            }
            else
            {
                Log.i(TAG, "HANDLE MESSAGE: SEND THREAD IS DEAD");
            }
            if (Looper.getMainLooper().getThread() == Thread.currentThread())
            {
                Log.i(TAG, "HANDLE MESSAGE1: LOOPER IS MAIN LOOPER");
            }
            else
            {
                Log.i(TAG, "HANDLE MESSAGE1: LOOPER IS NOT THE MAIN LOOPER");
            }

            try {
                InetAddress destAddr;

                int destPort;

                byte[] sendData;  // Combine sender and message text; default encoding is UTF-8

                ResultReceiver receiver;

                // TODO get data from message (including result receiver)

                String msg = (String) message.getData().get("msg");
                sendData = msg.getBytes();

                destAddr = (InetAddress) message.getData().getSerializable("address");

                destPort = (int) message.getData().get("port");

                receiver = message.getData().getParcelable("receiver");

                // End todo


                DatagramPacket sendPacket = new DatagramPacket(sendData,
                        sendData.length, destAddr, destPort);

                Log.i(TAG, "About to send packet message: " + msg);

                chatSocket.send(sendPacket);

                Log.i(TAG, "Sent packet: " + msg+" to port "+destPort);

                receiver.send(RESULT_OK, null);

            } catch (UnknownHostException e) {
                Log.e(TAG, "Unknown host exception: " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "IO exception: " + e.getMessage());
            }

        }
    }

    private final class ReceiverThread implements Runnable {

        public void run() {
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            while (!finished && socketOK) {

                try {

                    chatSocket.receive(receivePacket);
                    Log.i(TAG, "Received a packet");

                    InetAddress sourceIPAddress = receivePacket.getAddress();
                    Log.i(TAG, "Source IP Address: " + sourceIPAddress);

                    String msgContents[] = new String(receivePacket.getData(), 0, receivePacket.getLength()).split(":");

                    final ChatMessage message = new ChatMessage();
                    message.sender = msgContents[0];
                    message.timestamp = new Date(Long.parseLong(msgContents[1]));
                    message.messageText = msgContents[2];

                    Log.i(TAG, "Received from " + message.sender + ": " + message.messageText);

                    Peer sender = new Peer();
                    sender.name = message.sender;
                    sender.timestamp = message.timestamp;
                    sender.address = InetAddresses.toAddrString(receivePacket.getAddress());
                    sender.port = receivePacket.getPort();

                    peerManager.persistAsync(sender, new IContinue<Uri>() {
                        @Override
                        public void kontinue(Uri uri) {
                            message.senderId = (int) PeerContract.getId(uri);
                            messageManager.persistAsync(message);
                        }
                    });

                } catch (Exception e) {

                    Log.e(TAG, "Problems receiving packet.", e);
                    socketOK = false;
                }

            }

        }

    }

}
