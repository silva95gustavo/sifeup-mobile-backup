package pt.up.fe.mobile.ui.services;

import pt.up.fe.mobile.ui.BaseSinglePaneActivity;
import android.support.v4.app.Fragment;

public class ChangePasswordActivity extends BaseSinglePaneActivity
{
	@Override
    protected Fragment onCreatePane() 
	{	
        return new ChangePasswordFragment();
    }

}