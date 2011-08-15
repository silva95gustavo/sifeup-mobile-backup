package pt.up.fe.mobile.ui.studentarea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pt.up.fe.mobile.R;
import pt.up.fe.mobile.service.SessionManager;
import pt.up.fe.mobile.service.SifeupAPI;
import pt.up.fe.mobile.ui.BaseActivity;
import pt.up.fe.mobile.ui.BaseFragment;

import external.com.google.android.apps.iosched.util.AnalyticsUtils;
import external.com.google.android.apps.iosched.util.UIUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SubjectDescriptionFragment extends BaseFragment implements OnItemClickListener {
	
    private ExpandableListView descriptionList;
	
	 @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/Subject Description");
    }
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
		
		
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.subject_description, null);
		descriptionList = (ExpandableListView) root.findViewById(R.id.subject_description_list);
  
        new SubjectDescriptionTask().execute();
		return root;
	}
 
    /**
     * Represents a subject description.
     * Holds all data about it.
     *
     */
    private class SubjectDescription{
		public String acronym; // "EICXXXX"
		public int year; // 3
		public String namePt; // Sistemas Distribuidos
		public String nameEn; // Distributed Systems
		public String semester; // 2S
    }
    
    /** 
	 * Subject Description Parser
	 * Stores Description of Subject in @link{SubjectDescriptionFragment}
	 * Returns true in case of correct parsing.
	 * 
	 * @param page
	 * @return boolean
	 * @throws JSONException
	 */
    public boolean JSONSubjects(String page) throws JSONException{
    	JSONObject jObject = new JSONObject(page);
    	    	
    	if(jObject.has("inscricoes")){
    		Log.e("JSON", "founded subject description");
    		JSONArray jArray = jObject.getJSONArray("inscricoes");
    		
    		// if year number is wrong, returns false
    		if(jArray.length()==0)
    			return false;
    		
    		// iterate over jArray
    		for(int i = 0; i < jArray.length(); i++){
    			// new JSONObject
    			JSONObject jSubject = jArray.getJSONObject(i);
    			// new Block
    			SubjectDescription subject = new SubjectDescription();
    			
    			if(jSubject.has("dis_codigo")) subject.acronym = jSubject.getString("dis_codigo"); // Monday is index 0
    			if(jSubject.has("ano_curricular")) subject.year = jSubject.getInt("ano_curricular");
    			if(jSubject.has("nome")) subject.namePt = jSubject.getString("nome");
    			if(jSubject.has("name")) subject.nameEn = jSubject.getString("name");
    			if(jSubject.has("periodo")) subject.semester = jSubject.getString("periodo");
    		}
    		Log.e("JSON", "loaded subject description");
    		return true;
    	}
    	Log.e("JSON", "subject description not found");
    	return false;
    }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		StringBuilder url = new StringBuilder("https://www.fe.up.pt/si/disciplinas_geral.formview?");
	//	url.append("p_cad_codigo="+subjects.get(position).acronym);
		int secondYear = UIUtils.secondYearOfSchoolYear();
		int firstYear = secondYear -1;
		url.append("&p_ano_lectivo=" + firstYear +"/" + secondYear);
	//	url.append("&p_periodo=" +subjects.get(position).semester );
		Uri uri = Uri.parse( url.toString() );
		startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
	}
	
    /** 
     * Private class to fetch data to server
     * 
     * @author Ângela Igreja
     * 
     */
    private class SubjectDescriptionTask extends AsyncTask<Void, Void, String> {

    	protected void onPreExecute (){
    		showLoadingScreen();
    	}

        protected void onPostExecute(String result) {
			if ( getActivity() == null )
				 return;
        	if ( result.equals("Success") )
        	{
				Log.e("Subjects","success");
				
				 try {
					 String[] from = new String[] {"chair", "time", "room"};
			         int[] to = new int[] { R.id.exam_chair, R.id.exam_time, R.id.exam_room};
				     // prepare the list of all records
			         List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
			        /* for(Subject s : subjects){
			             HashMap<String, String> map = new HashMap<String, String>();
			             map.put("chair", s.namePt);
			             map.put("time", s.acronym + " (" + s.nameEn + ")");
			             map.put("room", getString(R.string.subjects_year,s.year, s.semester));
			             fillMaps.add(map);
			         }*/
			         // fill in the grid_item layout
			         SimpleAdapter adapter = new SimpleAdapter(getActivity(), fillMaps, R.layout.list_item_exam, from, to);
			        // list.setAdapter(adapter);
			        // list.setOnItemClickListener(SubjectDescriptionFragment.this);
			         showMainScreen();
			         Log.e("JSON", "subjects visual list loaded");
				 }
				 catch (Exception ex){
					 ex.printStackTrace();
					 if ( getActivity() != null )
							Toast.makeText(getActivity(), "F*** Fragments", Toast.LENGTH_LONG).show();

				 }
    		}
			else if ( result.equals("Error") ){	
				Log.e("Login","error");
				if ( getActivity() != null ) 
				{
					Toast.makeText(getActivity(), getString(R.string.toast_auth_error), Toast.LENGTH_LONG).show();
					((BaseActivity)getActivity()).goLogin(true);
					return;
				}
			}
			else if ( result.equals("") )
			{
				if ( getActivity() != null ) 	
				{
					Toast.makeText(getActivity(), getString(R.string.toast_server_error), Toast.LENGTH_LONG).show();
					getActivity().finish();
					return;
				}
			}
        }

		@Override
		protected String doInBackground(Void ... theVoid) {
			String page = "";
		  	try {
	    			page = SifeupAPI.getSubjectsReply(
								SessionManager.getInstance().getLoginCode(),
								"2010");
	    			int error =	SifeupAPI.JSONError(page);
		    		switch (error)
		    		{
		    			case SifeupAPI.Errors.NO_AUTH:
		    				return "Error";
		    			case SifeupAPI.Errors.NO_ERROR:
		    				JSONSubjects(page);
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