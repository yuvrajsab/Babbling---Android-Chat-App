package io.github.yuvrajsab.babblin;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.scottyab.aescrypt.AESCrypt;
import com.squareup.picasso.Picasso;

import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> MessagesList;
    private FirebaseAuth auth;
    private int writePermission = 12;

    public MessageAdapter(List<Messages> messagesList) {
        this.MessagesList = messagesList;
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_single_layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        String current_uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        Messages messages = MessagesList.get(position);
        String from_user = messages.getFrom();

        if (from_user.equals(current_uid)) {
            holder.chatLayout.setBackgroundResource(R.drawable.corner_chat_shape);
            holder.linearLayout.setGravity(Gravity.END);

        } else {
            holder.chatLayout.setBackgroundResource(R.drawable.corner_chat_other_shape);
            holder.linearLayout.setGravity(Gravity.START);
        }

        if (messages.getType().equals("text")) {
            String password = "Babblin-Chat";
            String encryptedMsg = messages.getMessage();
            String msg = null;

            try {
                msg = AESCrypt.decrypt(password, encryptedMsg);
            } catch (GeneralSecurityException e) {
                Toast.makeText(holder.itemView.getContext(), "Some error occured,\nPlease report error to us in Report Problem section.", Toast.LENGTH_LONG).show();
            }

            if (msg != null) {
                holder.messageText.setText(msg);
            }
            holder.messageImage.setVisibility(View.GONE);
        } else if (messages.getType().equals("image")) {
            holder.messageImage.setVisibility(View.VISIBLE);
            holder.messageText.setVisibility(View.GONE);
            Picasso.get().load(messages.getMessage()).placeholder(R.drawable.ic_image_black_48dp).into(holder.messageImage);
            final Uri uri = Uri.parse(messages.getMessage());

            holder.messageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu saveImgMenu = new PopupMenu(holder.itemView.getContext(), v);
                    saveImgMenu.getMenuInflater().inflate(R.menu.save_image_menu, saveImgMenu.getMenu());
                    saveImgMenu.show();

                    saveImgMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.saveImage) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (ContextCompat.checkSelfPermission(holder.itemView.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        // Permission is not granted
                                        ActivityCompat.requestPermissions((Activity) holder.itemView.getContext(),
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                writePermission);
                                    } else {
                                        DownloadManager downloadManager = (DownloadManager) holder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                        DownloadManager.Request request = new DownloadManager.Request(uri);

                                        request.setTitle(uri.getLastPathSegment());
                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                        request.setDestinationInExternalPublicDir("Babblin/images", uri.getLastPathSegment());

                                        if (downloadManager != null) {
                                            Long reference = downloadManager.enqueue(request);
                                        }
                                    }
                                } else {
                                    DownloadManager downloadManager = (DownloadManager) holder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                    DownloadManager.Request request = new DownloadManager.Request(uri);

                                    request.setTitle(uri.getLastPathSegment());
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setDestinationInExternalPublicDir("Babblin/images", uri.getLastPathSegment());

                                    if (downloadManager != null) {
                                        Long reference = downloadManager.enqueue(request);
                                    }
                                }
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });
                }
            });

        } else if (messages.getType().equals("other")) {
            holder.messageImage.setVisibility(View.VISIBLE);
            holder.messageText.setVisibility(View.GONE);
            holder.messageImage.setImageResource(R.drawable.ic_insert_drive_file_black_48dp);
            final Uri uri = Uri.parse(messages.getMessage());

            holder.messageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu saveMenu = new PopupMenu(holder.itemView.getContext(), v);
                    saveMenu.getMenuInflater().inflate(R.menu.save_menu, saveMenu.getMenu());
                    saveMenu.show();

                    saveMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.saveFile) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (ContextCompat.checkSelfPermission(holder.itemView.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        // Permission is not granted
                                        ActivityCompat.requestPermissions((Activity) holder.itemView.getContext(),
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                writePermission);
                                    } else {
                                        DownloadManager downloadManager = (DownloadManager) holder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                        DownloadManager.Request request = new DownloadManager.Request(uri);

                                        request.setTitle(uri.getLastPathSegment());
                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                        request.setDestinationInExternalPublicDir("Babblin/files", uri.getLastPathSegment());

                                        if (downloadManager != null) {
                                            Long reference = downloadManager.enqueue(request);
                                        }
                                    }
                                } else {
                                    DownloadManager downloadManager = (DownloadManager) holder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                    DownloadManager.Request request = new DownloadManager.Request(uri);

                                    request.setTitle(uri.getLastPathSegment());
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setDestinationInExternalPublicDir("Babblin/files", uri.getLastPathSegment());

                                    if (downloadManager != null) {
                                        Long reference = downloadManager.enqueue(request);
                                    }
                                }
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });
                }
            });
        }

        long timestamp = messages.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.ENGLISH);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        holder.messageTime.setText(dateFormat.format(calendar.getTime()));
    }

    @Override
    public int getItemCount() {
        return MessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView messageTime;
        ConstraintLayout chatLayout;
        LinearLayout linearLayout;
        ImageView messageImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.current_chat_text);
            messageTime = itemView.findViewById(R.id.current_chat_time);
            chatLayout = itemView.findViewById(R.id.chatLayout);
            linearLayout = itemView.findViewById(R.id.chatLinearLayout);
            messageImage = itemView.findViewById(R.id.current_chat_image);
        }
    }
}
