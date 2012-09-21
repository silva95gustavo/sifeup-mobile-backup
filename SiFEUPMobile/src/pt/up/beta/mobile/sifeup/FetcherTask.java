package pt.up.beta.mobile.sifeup;

import java.io.IOException;

import org.apache.http.auth.AuthenticationException;

import pt.up.beta.mobile.sifeup.ResponseCommand.ERROR_TYPE;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.AsyncTask;

public class FetcherTask<T> extends
		AsyncTask<String, Void, ResponseCommand.ERROR_TYPE> {

	private final ResponseCommand<T> command;
	private final ParserCommand<T> parser;
	private final Context context;
	private T result;

	public FetcherTask(ResponseCommand<T> com, ParserCommand<T> parser, Context context) {
		this.command = com;
		this.parser = parser;
		this.context = context;
	}

	protected void onPostExecute(ERROR_TYPE error) {
		if (error == null) {
			command.onResultReceived(this.result);
			return;
		}
		command.onError(error);
	}

	protected ERROR_TYPE doInBackground(String... pages) {
		String page = "";
		try {
			if (pages.length < 1)
				return ERROR_TYPE.GENERAL;
			if (isCancelled())
				return null;
			page = SifeupAPI.getReply(pages[0],
					AccountUtils.getAuthToken(context), context);
			result = parser.parse(page);
		} catch (OperationCanceledException e) {
			e.printStackTrace();
			return ERROR_TYPE.AUTHENTICATION;
		} catch (AuthenticatorException e) {
			e.printStackTrace();
			return ERROR_TYPE.AUTHENTICATION;
		} catch (IOException e) {
			e.printStackTrace();
			return ERROR_TYPE.NETWORK;
		} catch (AuthenticationException e) {
			e.printStackTrace();
			return ERROR_TYPE.AUTHENTICATION;
		}

		return null;
	}

}
