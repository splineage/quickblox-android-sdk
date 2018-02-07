package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.TimeUtils;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.chat.utils.qb.PaginationHistoryListener;
import com.quickblox.sample.chat.utils.qb.QbUsersHolder;
import com.quickblox.sample.core.utils.ResourceUtils;
import com.quickblox.ui.kit.chatmessage.adapter.QBMessagesAdapter;
import com.quickblox.ui.kit.chatmessage.adapter.utils.LinkUtils;
import com.quickblox.users.model.QBUser;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.List;

/**
 * Created by roman on 2/5/18.
 */

public class ChatRecycleViewAdapter extends QBMessagesAdapter<QBChatMessage> implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    private static final String TAG = ChatRecycleViewAdapter.class.getSimpleName();
    private final QBChatDialog chatDialog;
    private PaginationHistoryListener paginationListener;
    private int previousGetCount = 0;

    public ChatRecycleViewAdapter(Context context, QBChatDialog chatDialog, List<QBChatMessage> chatMessages) {
        super(context, chatMessages);
        this.chatDialog = chatDialog;
    }

    public void addToList(List<QBChatMessage> items) {
        chatMessages.addAll(0, items);
        notifyItemRangeInserted(0, items.size());
    }

    @Override
    public void onBindViewHolder(QBMessageViewHolder holder, int position) {
        downloadMore(position);
        QBChatMessage chatMessage = getItem(position);
        if (isIncoming(chatMessage) && !isRead(chatMessage)) {
            readMessage(chatMessage);
        }
        super.onBindViewHolder(holder, position);
    }

    @Override
    public String getImageUrl(int position) {
        QBAttachment attachment = getQBAttach(position);
        return attachment.getUrl();
    }

    @Override
    protected void onBindViewMsgLeftHolder(TextMessageHolder holder, QBChatMessage chatMessage, int position) {
        holder.timeTextMessageTextView.setVisibility(View.GONE);

        QBUser sender = QbUsersHolder.getInstance().getUserById(chatMessage.getSenderId());
        String senderName = sender.getFullName();

        TextView opponentNameTextView = holder.itemView.findViewById(R.id.opponent_name_text_view);
        opponentNameTextView.setText(senderName);

        TextView customMessageTimeTextView = holder.itemView.findViewById(R.id.custom_msg_text_time_message);
        customMessageTimeTextView.setText(getDate(chatMessage.getDateSent()));

        super.onBindViewMsgLeftHolder(holder, chatMessage, position);
    }

    protected void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    private void readMessage(QBChatMessage chatMessage) {
        try {
            chatDialog.readMessage(chatMessage);
        } catch (XMPPException | SmackException.NotConnectedException e) {
            Log.w(TAG, e);
        }
    }

    private boolean isRead(QBChatMessage chatMessage) {
        Integer currentUserId = ChatHelper.getCurrentUser().getId();
        return !CollectionsUtil.isEmpty(chatMessage.getReadIds()) && chatMessage.getReadIds().contains(currentUserId);
    }

    public void setPaginationHistoryListener(PaginationHistoryListener paginationListener) {
        this.paginationListener = paginationListener;
    }

    private void downloadMore(int position) {
        if (position == 0) {
            if (getItemCount() != previousGetCount) {
                paginationListener.downloadMore();
                previousGetCount = getItemCount();
            }
        }
    }

    @Override
    public long getHeaderId(int position) {
        QBChatMessage chatMessage = getItem(position);
        return TimeUtils.getDateAsHeaderId(chatMessage.getDateSent() * 1000);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        Log.w(TAG, "onCreateHeaderViewHolder= " + parent);
        View view = inflater.inflate(R.layout.view_chat_message_header, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        View view = holder.itemView;
        Log.w(TAG, "onBindHeaderViewHolder position= " + position);
        TextView dateTextView = view.findViewById(R.id.header_date_textview);

        QBChatMessage chatMessage = getItem(position);
        dateTextView.setText(TimeUtils.getDate(chatMessage.getDateSent() * 1000));

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) dateTextView.getLayoutParams();
        if (position == 0) {
            lp.topMargin = ResourceUtils.getDimen(R.dimen.chat_date_header_top_margin);
        } else {
            lp.topMargin = 0;
        }
        dateTextView.setLayoutParams(lp);
    }
}
