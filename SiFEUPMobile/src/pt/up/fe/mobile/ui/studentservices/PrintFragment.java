

package pt.up.fe.mobile.ui.studentservices;


import external.com.google.android.apps.iosched.util.AnalyticsUtils;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import pt.up.fe.mobile.R;
import pt.up.fe.mobile.service.SessionManager;
import pt.up.fe.mobile.service.SifeupAPI;
import pt.up.fe.mobile.ui.BaseActivity;

public class PrintFragment extends Fragment {

    private String saldo;
    private TextView display;
    private TextView desc;
    public String getSaldo() {
		return saldo;
	}

	public  void setSaldo(String saldo) {
		this.saldo = saldo;
	}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/Printing");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	new PrintTask().execute();
    	ViewGroup root = (ViewGroup) inflater.inflate(R.layout.print_balance, null);
    	display = ((TextView)root.findViewById(R.id.print_balance));
    	desc = ((TextView)root.findViewById(R.id.print_desc));
    	final EditText value = (EditText)root.findViewById(R.id.print_value);
    	root.findViewById(R.id.print_generate_reference).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String newValue = value.getText().toString().trim();
				try{
					Double.valueOf(newValue);
				}
				catch (NumberFormatException e) {
					Toast.makeText(getActivity(), getString(R.string.toast_auth_error), Toast.LENGTH_LONG).show();
					value.requestFocus();
					return;
				}
				newValue = newValue.replace(".",",");
				Intent i = new Intent(getActivity(), PrintRefActivity.class);
				i.putExtra("value", newValue);
				startActivity(i);
			}
		});
    	return root;

    }
    private class PrintTask extends AsyncTask<Void, Void, String> {

    	protected void onPreExecute (){
    		if ( getActivity() != null ) 
    			getActivity().showDialog(BaseActivity.DIALOG_FETCHING);  
    	}

        protected void onPostExecute(String saldo) {
        	if ( getActivity() == null )
        		return;
        	if ( saldo.equals("") )
        	{
        		if ( getActivity() != null ) 
				{
					getActivity().removeDialog(BaseActivity.DIALOG_FETCHING);
					Toast.makeText(getActivity(), getString(R.string.toast_server_error), Toast.LENGTH_LONG).show();
					getActivity().finish();
					return;
				}
			}
			else if ( saldo.equals("Error") ){	
				if ( getActivity() != null ) 
				{
					getActivity().removeDialog(BaseActivity.DIALOG_FETCHING);
					Toast.makeText(getActivity(), getString(R.string.toast_auth_error), Toast.LENGTH_LONG).show();
					((BaseActivity)getActivity()).goLogin(true);
					return;
				}
			}
			else{
				Log.e("Login","success");
				display.setText(getString(R.string.print_balance, saldo));
				PrintFragment.this.saldo = saldo;
				long pagesA4Black =  Math.round(Double.parseDouble(saldo) / 0.03f);
				if ( pagesA4Black > 0 )
					desc.setText(getString(R.string.print_can_print_a4_black, Long.toString(pagesA4Black)));
			}
				
        	if ( getActivity() != null ) 
        		getActivity().removeDialog(BaseActivity.DIALOG_FETCHING);
        }

		@Override
		protected String doInBackground(Void ... theVoid) {
			String page = "";
			try {
	    			page = SifeupAPI.getPrintingReply(
								SessionManager.getInstance().getLoginCode());
	    		
	    			int error =	SifeupAPI.JSONError(page);
		    		switch (error)
		    		{
		    			case SifeupAPI.Errors.NO_AUTH:
		    				return "Error";
		    			case SifeupAPI.Errors.NO_ERROR:
		    				return new JSONObject(page).optString("saldo");
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