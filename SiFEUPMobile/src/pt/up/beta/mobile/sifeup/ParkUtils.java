package pt.up.beta.mobile.sifeup;

import org.acra.ACRA;
import org.json.JSONException;

import pt.up.beta.mobile.datatypes.Park;
import pt.up.beta.mobile.sifeup.ResponseCommand.ERROR_TYPE;
import android.content.Context;
import android.os.AsyncTask;

public class ParkUtils {
	private ParkUtils() {
	}

	public static AsyncTask<String, Void, ERROR_TYPE> getParkReply( String code , 
			ResponseCommand<Park> command, Context context) {
		return new FetcherTask<Park>(command, new ParkParser(), context).execute(SifeupAPI
				.getParkOccupationUrl(code));
	}

	/**
	 * Parses a JSON String containing Exams info, Stores that info at
	 * Collection exams.
	 */

	private static class ParkParser implements ParserCommand<Park> {

		public Park parse(String page) {
			try {
				return new Park().JSONParkOccupation(page);
			} catch (JSONException e) {
				e.printStackTrace();
				ACRA.getErrorReporter().handleSilentException(e);
				ACRA.getErrorReporter().handleSilentException(
						new RuntimeException("Id:"
								+ AccountUtils
										.getActiveUserCode(null)
								+ "\n\n" + page));
			}
			return null;
		}

	}
}
