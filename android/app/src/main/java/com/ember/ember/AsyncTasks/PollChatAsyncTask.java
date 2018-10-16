package com.ember.ember.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.ember.ember.activity.ChatActivity;
import com.ember.ember.adapter.ChatRecycler;
import com.ember.ember.helper.http.ErrorHelper;
import com.ember.ember.helper.http.HttpHelper;
import com.ember.ember.model.Chat;
import com.ember.ember.model.ChatList;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PollChatAsyncTask extends AsyncTask<Object, Void, Void> {

    private int lastUpdate = 0;
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected Void doInBackground(Object... objects) {
        RecyclerView recyclerView = (RecyclerView) objects[0];
        ChatRecycler chatRecycler = (ChatRecycler) recyclerView.getAdapter();
        String me = objects[1].toString();
        String other = objects[2].toString();
        Context context = (Context) objects[3];
        while (true) {
            Call<ChatList> call = HttpHelper.getChat(me, other);
            call.enqueue(new Callback<ChatList>() {
                @Override
                public void onResponse(Call<ChatList> call, Response<ChatList> response) {
                    if (response.isSuccessful()) {
                        try {
                            List<String> allChats = response.body().getData();
                            addToChat(allChats, recyclerView, chatRecycler, me);
                            lastUpdate = allChats.size();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        ErrorHelper.raiseToast(context, ErrorHelper.Problem.CALL_FAILED);
                    }
                }

                @Override
                public void onFailure(Call<ChatList> call, Throwable t) {
                    ErrorHelper.raiseToast(context, ErrorHelper.Problem.CALL_FAILED);
                }
            });
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void addToChat(List<String> allChats, RecyclerView recyclerView, ChatRecycler chatRecycler, String me) throws ParseException {
        List<String> newChats;
        newChats = allChats.subList(lastUpdate, allChats.size());
        lastUpdate = allChats.size();
        List<Chat> toAdd = new ArrayList<>();
        for (String chatStr : newChats) {
            String[] chatArr = chatStr.split("\\|");
            Date timestamp = df.parse(chatArr[0] + " " + chatArr[1]);
            String sender = chatArr[2];
            String message = chatArr[4];
            Chat chat = new Chat(sender.equals(me), message, timestamp);
            toAdd.add(chat);
        }
        chatRecycler.addToChat(toAdd);
        if (!recyclerView.canScrollVertically(1)) {
            recyclerView.scrollToPosition(chatRecycler.getItemCount() - 1);
        }
    }
}
