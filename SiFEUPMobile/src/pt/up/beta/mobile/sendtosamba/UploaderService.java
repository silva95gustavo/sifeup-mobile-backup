package pt.up.beta.mobile.sendtosamba;

import java.io.ByteArrayInputStream;

import pt.up.beta.mobile.R;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class UploaderService extends Service implements FinishedTaskListener {
    /**
     * Username that should be the intent
     */
    public final static String USERNAME_KEY = "pt.up.fe.sendtosamba.USERNAME";

    /**
     * Password that should be the intent
     */
    public final static String PASSWORD_KEY = "pt.up.fe.sendtosamba.PASSWORD";

    int taskRunning = 0;

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        Log.i("UploaderService", "Received start id " + startId + ": " + i);

        Intent intent = i;
        if ( intent == null )
        {
        	if ( taskRunning == 0 )
        	{
	        	stopSelf();
	        	return START_NOT_STICKY;
        	}
        	else
        	{
        		return START_STICKY;
        	}
        }
        Bundle extras = intent.getExtras();
        final String username = intent.getStringExtra(USERNAME_KEY);
        final String password = intent.getStringExtra(PASSWORD_KEY);
        try {
            final InputStreamManaged is;
            Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
            CharSequence filename = null;
            if (uri == null) {
                filename = extras.getCharSequence(Intent.EXTRA_TEXT);
                if (filename == null) {
                    Toast.makeText(this,
                            getString(R.string.notification_unsupported),
                            Toast.LENGTH_SHORT).show();
                    if ( taskRunning == 0 )
                	{
        	        	stopSelf();
        	        	return START_NOT_STICKY;
                	}
                	else
                	{
                		return START_STICKY;
                	}
                }
                is = new InputStreamManaged(new ByteArrayInputStream(filename
                        .toString().getBytes("UTF-8")));
                is.setLength(filename.length());

                filename = extras.getCharSequence(Intent.EXTRA_SUBJECT);
                if (filename == null) {
                    filename = getString(R.string.app_name)
                            + System.currentTimeMillis();
                }
                filename = filename + ".txt";
            } else {
                ContentResolver cr = getContentResolver();
                is = new InputStreamManaged(cr.openInputStream(uri));
                final String path = getRealPathFromUri(this,uri);
                if ( path != null )
                {
                    is.setLength(new java.io.File(path).length());
                    int offset = path.lastIndexOf('/');
                    filename = path.substring(offset + 1);
                }
                else
                {
                    is.setLength(0);
                    filename = uri.toString();
                    int offset = filename.toString().lastIndexOf('/');
                    filename = filename.toString().substring(offset + 1);
                }
            }
            taskRunning++;
            final UploaderTask task = new UploaderTask(this, this, is,
                    filename.toString());
            task.execute(username, password);

        } catch (Exception e) {
            e.printStackTrace();
        }
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LoggerServiceBinder extends Binder {
        public UploaderService getService() {
            return UploaderService.this;
        }
    }

    public IBinder onBind(Intent arg0) {
        return new LoggerServiceBinder();
    }

    public void finishedTask() {
        taskRunning--;
        if (taskRunning == 0)
            stopSelf();
    };
    
    public static String getRealPathFromUri(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if ( cursor == null )
            return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        final String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

}