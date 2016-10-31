package amap.com.android_path_record;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceListener;
import com.amap.api.trace.TraceLocation;
import com.amap.api.trace.TraceOverlay;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import amap.com.database.DbAdapter;
import amap.com.record.PathRecord;
import amap.com.recorduitl.Util;


public class MainActivity extends Activity implements LocationSource,
		AMapLocationListener, TraceListener {
	private final static int CALLTRACE = 0;
	private MapView mMapView;
	private AMap mAMap;
	private OnLocationChangedListener mListener;
	private AMapLocationClient mLocationClient;
	private AMapLocationClientOption mLocationOption;
	private PolylineOptions mPolyoptions, tracePolytion;
	private Polyline mpolyline;
	private PathRecord record;
	private long mStartTime;
	private long mEndTime;
	private ToggleButton btn;
	private DbAdapter DbHepler;
	private List<TraceLocation> mTracelocationlist = new ArrayList<TraceLocation>();
	private int tracesize = 10;
	private TraceOverlay mTraceoverlay;
	private List<TraceOverlay> mOverlayList = new ArrayList<TraceOverlay>();
	private TextView mResultShow;
	private List<AMapLocation> recordList = new ArrayList<AMapLocation>();
	private Marker mlocMarker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basicmap_activity);
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.onCreate(savedInstanceState);// 此方法必须重写
		init();
		initpolyline();
	}

	/**
	 * 初始化AMap对象
	 */
	private void init() {
		if (mAMap == null) {
			mAMap = mMapView.getMap();
			setUpMap();
		}
		btn = (ToggleButton) findViewById(R.id.locationbtn);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (btn.isChecked()) {
					mAMap.clear(true);
					if (record != null) {
						record = null;
					}
					record = new PathRecord();
					mStartTime = System.currentTimeMillis();
					record.setDate(getcueDate(mStartTime));
					mResultShow.setText("总距离");
				} else {
					mEndTime = System.currentTimeMillis();
					mOverlayList.add(mTraceoverlay);
					DecimalFormat decimalFormat = new DecimalFormat("0.0");
					mResultShow.setText(
							decimalFormat.format(getTotalDistance() / 1000d) + "KM");
//					LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
//					mTraceClient.queryProcessedTrace(2, Util.parseTraceLocationList(record.getPathline()) , LBSTraceClient.TYPE_AMAP, MainActivity.this);
					saveRecord(record.getPathline(), record.getDate());
				}
			}
		});
		mResultShow = (TextView) findViewById(R.id.show_all_dis);

		mTraceoverlay = new TraceOverlay(mAMap);
	}

	protected void saveRecord(List<AMapLocation> list, String time) {
		if (list != null && list.size() > 0) {
			DbHepler = new DbAdapter(this);
			DbHepler.open();
			String duration = getDuration();
			float distance = getDistance(list);
			String average = getAverage(distance);
			String pathlineSring = getPathLineString(list);
			AMapLocation firstLocaiton = list.get(0);
			AMapLocation lastLocaiton = list.get(list.size() - 1);
			String stratpoint = amapLocationToString(firstLocaiton);
			String endpoint = amapLocationToString(lastLocaiton);
			DbHepler.createrecord(String.valueOf(distance), duration, average,
					pathlineSring, stratpoint, endpoint, time);
			DbHepler.close();
		} else {
			Toast.makeText(MainActivity.this, "没有记录到路径", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private String getDuration() {
		return String.valueOf((mEndTime - mStartTime) / 1000f);
	}

	private String getAverage(float distance) {
		return String.valueOf(distance / (float) (mEndTime - mStartTime));
	}

	private float getDistance(List<AMapLocation> list) {
		float distance = 0;
		if (list == null || list.size() == 0) {
			return distance;
		}
		for (int i = 0; i < list.size() - 1; i++) {
			AMapLocation firstpoint = list.get(i);
			AMapLocation secondpoint = list.get(i + 1);
			LatLng firstLatLng = new LatLng(firstpoint.getLatitude(),
					firstpoint.getLongitude());
			LatLng secondLatLng = new LatLng(secondpoint.getLatitude(),
					secondpoint.getLongitude());
			double betweenDis = AMapUtils.calculateLineDistance(firstLatLng,
					secondLatLng);
			distance = (float) (distance + betweenDis);
		}
		return distance;
	}

	private String getPathLineString(List<AMapLocation> list) {
		if (list == null || list.size() == 0) {
			return "";
		}
		StringBuffer pathline = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			AMapLocation location = list.get(i);
			String locString = amapLocationToString(location);
			pathline.append(locString).append(";");
		}
		String pathLineString = pathline.toString();
		pathLineString = pathLineString.substring(0,
				pathLineString.length() - 1);
		return pathLineString;
	}

	private String amapLocationToString(AMapLocation location) {
		StringBuffer locString = new StringBuffer();
		locString.append(location.getLatitude()).append(",");
		locString.append(location.getLongitude()).append(",");
		locString.append(location.getProvider()).append(",");
		locString.append(location.getTime()).append(",");
		locString.append(location.getSpeed()).append(",");
		locString.append(location.getBearing());
		return locString.toString();
	}

	private void initpolyline() {
		mPolyoptions = new PolylineOptions();
		mPolyoptions.width(10f);
		mPolyoptions.color(Color.GRAY);
		tracePolytion = new PolylineOptions();
		tracePolytion.width(40);
		tracePolytion.setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.grasp_trace_line));
	}

	/**
	 * 设置一些amap的属性
	 */
	private void setUpMap() {
		mAMap.setLocationSource(this);// 设置定位监听
		mAMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		mAMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
		// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
		mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		startlocation();
	}

	@Override
	public void deactivate() {
		mListener = null;
		if (mLocationClient != null) {
			mLocationClient.stopLocation();
			mLocationClient.onDestroy();

		}
		mLocationClient = null;
	}

	/**
	 * 定位结果回调
	 * @param amapLocation 位置信息类
     */
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (mListener != null && amapLocation != null) {
			if (amapLocation != null && amapLocation.getErrorCode() == 0) {
				mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
				LatLng mylocation = new LatLng(amapLocation.getLatitude(),
						amapLocation.getLongitude());
				mAMap.moveCamera(CameraUpdateFactory.changeLatLng(mylocation));
				if (btn.isChecked()) {
					record.addpoint(amapLocation);
					mPolyoptions.add(mylocation);
					mTracelocationlist.add(Util.parseTraceLocation(amapLocation));
					redrawline();
					if (mTracelocationlist.size() > tracesize - 1) {
						trace();
					}
				}
			} else {
				String errText = "定位失败," + amapLocation.getErrorCode() + ": "
						+ amapLocation.getErrorInfo();
				Log.e("AmapErr", errText);
			}
		}
	}

	/**
	 * 开始定位。
	 */
	private void startlocation() {
		if (mLocationClient == null) {
			mLocationClient = new AMapLocationClient(this);
			mLocationOption = new AMapLocationClientOption();
			// 设置定位监听
			mLocationClient.setLocationListener(this);
			// 设置为高精度定位模式
			mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);

			mLocationOption.setInterval(2000);

			mLocationOption.setMockEnable(true);
			// 设置定位参数
			mLocationClient.setLocationOption(mLocationOption);
			// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
			// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
			// 在定位结束后，在合适的生命周期调用onDestroy()方法
			// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
			mLocationClient.startLocation();

		}
	}

	/**
	 * 实时轨迹画线
	 */
	private void redrawline() {
		if (mPolyoptions.getPoints().size() > 1) {
			if (mpolyline != null) {
				mpolyline.setPoints(mPolyoptions.getPoints());
			} else {
				mpolyline = mAMap.addPolyline(mPolyoptions);
			}
		}
//		if (mpolyline != null) {
//			mpolyline.remove();
//		}
//		mPolyoptions.visible(true);
//		mpolyline = mAMap.addPolyline(mPolyoptions);
//			PolylineOptions newpoly = new PolylineOptions();
//			mpolyline = mAMap.addPolyline(newpoly.addAll(mPolyoptions.getPoints()));
//		}
	}

	@SuppressLint("SimpleDateFormat")
	private String getcueDate(long time) {
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd  HH:mm:ss ");
		Date curDate = new Date(time);
		String date = formatter.format(curDate);
		return date;
	}

	public void record(View view) {
		Intent intent = new Intent(MainActivity.this, RecordActivity.class);
		startActivity(intent);
	}

	private void trace() {
		List<TraceLocation> locationList = new ArrayList<>(mTracelocationlist);
		LBSTraceClient mTraceClient = new LBSTraceClient(getApplicationContext());
		mTraceClient.queryProcessedTrace(1, locationList, LBSTraceClient.TYPE_AMAP, this);
		TraceLocation lastlocation = mTracelocationlist.get(mTracelocationlist.size()-1);
		mTracelocationlist.clear();
		mTracelocationlist.add(lastlocation);
	}

	/**
	 * 轨迹纠偏失败回调。
	 * @param i
	 * @param s
     */
	@Override
	public void onRequestFailed(int i, String s) {
		mOverlayList.add(mTraceoverlay);
		mTraceoverlay = new TraceOverlay(mAMap);
	}

	@Override
	public void onTraceProcessing(int i, int i1, List<LatLng> list) {

	}

	/**
	 * 轨迹纠偏成功回调。
	 * @param lineID 纠偏的线路ID
	 * @param linepoints 纠偏结果
	 * @param distace 总距离
	 * @param watingtime 等待时间
     */
	@Override
	public void onFinished(int lineID, List<LatLng> linepoints, int distace, int watingtime) {
		if (lineID == 1) {
			if (linepoints != null && linepoints.size()>0) {
				mTraceoverlay.add(linepoints);
				mTraceoverlay.setDistance(mTraceoverlay.getDistance()+distace);
				if (mlocMarker == null) {
					mlocMarker = mAMap.addMarker(new MarkerOptions().position(linepoints.get(linepoints.size() - 1)).title("距离：" + getTotalDistance()+"米"));
					mlocMarker.showInfoWindow();
				} else {
					mlocMarker.setPosition(linepoints.get(linepoints.size() - 1));
					mlocMarker.setTitle("距离：" + getTotalDistance()+"米");
				}
			}
		} else if (lineID == 2) {
			if (linepoints != null && linepoints.size()>0) {
				mAMap.addPolyline(new PolylineOptions()
						.color(Color.RED)
						.width(40).addAll(linepoints));
			}
		}

	}

	/**
	 * 获取总距离
	 * @return
     */
	private int getTotalDistance() {
		int distance = 0;
		for (TraceOverlay to : mOverlayList) {
			distance = distance + to.getDistance();
		}
		return distance;
	}
}
