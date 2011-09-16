package pt.up.fe.mobile.ui.studentservices;

import org.json.JSONException;

import pt.up.fe.mobile.R;
import pt.up.fe.mobile.service.PasswordCheck;
import pt.up.fe.mobile.service.SessionManager;
import pt.up.fe.mobile.service.SifeupAPI;
import pt.up.fe.mobile.ui.BaseActivity;
import pt.up.fe.mobile.ui.BaseFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import external.com.google.android.apps.iosched.util.AnalyticsUtils;


/**
 * Change Password Fragment
 * 
 * @author Ângela Igreja
 *
 */
public class ChangePasswordFragment extends BaseFragment
{
	private EditText actualPasswordText;
	private EditText usernameText;
	private EditText newPasswordText;
	private EditText confirmNewPasswordText;
	private PasswordCheck checker;
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
       super.onCreate(savedInstanceState);
       checker = new PasswordCheck();//TODO: check if it is too slow
       AnalyticsUtils.getInstance(getActivity()).trackPageView("/Change Password");
    }
	 
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) 
	{
		super.onCreateView(inflater, container, savedInstanceState);
    	View root = inflater.inflate(R.layout.change_password, getParentContainer(), true);
    	
    	/** Cancel */
    	Button cancel = (Button) root.findViewById(R.id.set_password_cancel);
    	cancel.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				if ( getActivity() == null )
					return;
				getActivity().finish();
			}
		});
    	
    	/** Confirm */
    	//TODO: Apenas quando confirm ver a qualidade da password????
    	Button setPassword = (Button) root.findViewById(R.id.set_password_confirm);
    	setPassword.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				String username = usernameText.getText().toString();
		    	
		    	if(username.equals(""))
		    	{
		    		Toast.makeText(getActivity(), "Username can not be empty.",Toast.LENGTH_SHORT).show();
		    	}
		    	String actualPassword = actualPasswordText.getText().toString();
		    	
		    	if(actualPassword.equals(""))
		    	{
		    		Toast.makeText(getActivity(), "Current password can not be empty.",Toast.LENGTH_SHORT).show();
		    	}
				new PasswordTask().execute();
			}
		});
    	
    	/** Username */
    	usernameText = (EditText) root.findViewById(R.id.username);
 
    		
    	/** Current Password */
    	actualPasswordText = (EditText) root.findViewById(R.id.current_password);    	
    	
    	/** New Password */
    	newPasswordText = (EditText) root.findViewById(R.id.new_password);
    	
    	newPasswordText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
	
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				String password = s.toString();
    	        int result = checker.validatePassword(password);
    	        usernameText.setText(newPasswordText.getText().toString() + " Pass = " + result);
    	    }
    	});
    	
    	
    	/** Confirm New Password */
    	confirmNewPasswordText = (EditText) root.findViewById(R.id.confirm_new_password);
    	
    	
    	
    	showMainScreen();
 
		return getParentContainer();
	} 
    
    /** Classe privada para a busca de dados ao servidor */
    private class PasswordTask extends AsyncTask<Void, Void, String> {

    	protected void onPreExecute (){
    		if ( getActivity() != null )
    			getActivity().showDialog(BaseActivity.DIALOG_FETCHING);
    	}

        protected void onPostExecute(String result) {
			if ( getActivity() == null )
				 return;
        	if ( result.equals("Success") )
        	{

    		}
			else if ( result.equals("Error") ){	
			
			}
			else if ( result.equals("") )
			{

			}
        }

		@Override
		//TODO: passar argumentos
		protected String doInBackground(Void ... theVoid) {
			String page = "";
		  	try {
	    		   page = SifeupAPI.getSetPasswordReply(usernameText.getText().toString(),
	    				   								actualPasswordText.getText().toString(), 
	    				   								newPasswordText.getText().toString(),
	    				   								confirmNewPasswordText.getText().toString(), "S");
	    			int error =	SifeupAPI.JSONError(page);
		    		switch (error)
		    		{
		    			case SifeupAPI.Errors.NO_AUTH:
		    				return "Error";
		    			case SifeupAPI.Errors.NO_ERROR:

		    				return "Success";
		    			case SifeupAPI.Errors.NULL_PAGE:
		    				return "";	
		    		}

				return "";
			} catch (JSONException e) {
				if ( getActivity() != null ) 
					Toast.makeText(getActivity(), "F*** JSON", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}

			return "";
		}
    }
	  
	      
}

