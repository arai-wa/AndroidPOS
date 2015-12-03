package com.ricoh.pos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.caldroid.CaldroidFragment;
import com.caldroid.CaldroidGridAdapter;
import com.caldroid.CaldroidListener;
import com.ricoh.pos.data.SingleSalesRecord;
import com.ricoh.pos.model.SalesCalenderManager;
import com.ricoh.pos.model.SalesRecordManager;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class SalesCalenderActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_sales_calender);

		CaldroidFragment caldroidFragment = new SalesTrendFragment();
		Bundle args = new Bundle();
		Calendar cal = Calendar.getInstance();
		args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
		args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
		caldroidFragment.setArguments(args);

		addCalenderListener(caldroidFragment);

		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		t.replace(R.id.calendar1, caldroidFragment);
		t.commit();

	}

	private void addCalenderListener(CaldroidFragment fragment) {
		CaldroidListener listener = new CaldroidListener() {

			@Override
			public void onSelectDate(Date date, View view) {

				ArrayList<SingleSalesRecord> salesRecordsOfTheDay =
						SalesRecordManager.getInstance().restoreSingleSalesRecordsOfTheDay(date);

				if (salesRecordsOfTheDay.size() == 0) {
					// No sales record.
					// TODO: show alert dialog or something
					return;
				}

				SalesCalenderManager.getInstance().setSelectedDate(date);

				Intent intent = new Intent(SalesCalenderActivity.this, SalesRecordListActivity.class);
				startActivity(intent);
			}

		};

		fragment.setCaldroidListener(listener);
	}


	public static class SalesTrendFragment extends CaldroidFragment {

		@Override
		public CaldroidGridAdapter getNewDatesGridAdapter(int month, int year) {
			return new SalesTrendGridAdapter(getActivity(), month, year, getCaldroidData(), extraData);
		}

		private static class SalesTrendGridAdapter extends CaldroidGridAdapter {
			public SalesTrendGridAdapter(Context context, int month, int year, HashMap<String, Object> caldroidData, HashMap<String, Object> extraData) {
				super(context, month, year, caldroidData, extraData);
			}

			@Override
			protected void setCustomResources(DateTime dateTime, TextView textView) {
				super.setCustomResources(dateTime, textView);


				Date today = dateTime.toDate();

				double todaySales = SalesRecordManager.getInstance().getOneDayTotalSales(today);
				double todayProfit = SalesRecordManager.getInstance().getOneDayTotalNetProfit(today);

				textView.setText(textView.getText() + "\n\n" + todaySales + "\n" + todayProfit);


				double yesterdayProfit = SalesRecordManager.getInstance().getOneDayTotalNetProfit(dateTime.minusDays(1).toDate());

				if (yesterdayProfit < todayProfit) {
					textView.setBackgroundResource(R.color.profit_increased);
				} else if (todayProfit < yesterdayProfit) {
					textView.setBackgroundResource(R.color.profit_degreased);
				}
			}
		}
	}
}