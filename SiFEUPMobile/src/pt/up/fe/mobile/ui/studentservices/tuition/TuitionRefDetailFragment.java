/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pt.up.fe.mobile.ui.studentservices.tuition;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pt.up.fe.mobile.R;
import pt.up.fe.mobile.datatypes.RefMB;
import pt.up.fe.mobile.datatypes.YearsTuition;
import pt.up.fe.mobile.sifeup.*;
import pt.up.fe.mobile.tracker.AnalyticsUtils;

public class TuitionRefDetailFragment extends Fragment {

    private RefMB ref;
	private TextView nome;
	private TextView entidade;
	private TextView referencia;
	private TextView valor;
	private TextView dataIni;
	private TextView dataFim;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsUtils.getInstance(getActivity()).trackPageView("/ReferenceDetail");
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	ViewGroup root = (ViewGroup) inflater.inflate(R.layout.ref_mb, null);
    	
    	nome=(TextView)root.findViewById(R.id.tuition_ref_detail_name);
    	entidade = ((TextView)root.findViewById(R.id.tuition_ref_detail_entity));
    	referencia=(TextView)root.findViewById(R.id.tuition_ref_detail_reference);
    	valor=(TextView)root.findViewById(R.id.tuition_ref_detail_amount);
    	dataIni=(TextView)root.findViewById(R.id.tuition_ref_detail_date_start);
    	dataFim=(TextView)root.findViewById(R.id.tuition_ref_detail_date_end);
    	YearsTuition y=SessionManager.tuitionHistory.getHistory().get(SessionManager.tuitionHistory.currentYear);
        ref=y.getReferences().get(y.getSelectedReference());
        nome.setText(ref.getName());
        entidade.setText(""+ref.getEntity());
        referencia.setText(""+ref.getRef());
        valor.setText(ref.getAmount()+"€");
        dataIni.setText(ref.getStartDate().format3339(true));
        dataFim.setText(ref.getEndDate().format3339(true));
    	return root;

    }
}