/**  
 * Project Name:LocationService  
 * File Name:EmulatorService.java  
 * Package Name:com.amap.apitest  
 * Date:2015年11月27日下午1:39:49  
 *  
 */
/**  
 */

package amap.com.test;

import java.io.FileDescriptor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**  
 * ClassName:EmulatorService <br/>  
 * Function: TODO ADD FUNCTION. <br/>  
 * Reason:   TODO ADD REASON. <br/>  
 * Date:     2015年11月27日 下午1:39:49 <br/>  
 * @author   yiyi.qi  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
/**  
 */
public class EmulatorService extends Service {
	public static double latitude;
	public static double longitude;

	public boolean isMockOpen() {

		String strMock = Settings.Secure.ALLOW_MOCK_LOCATION;
		strMock = Settings.Secure.getString(getApplicationContext()
				.getContentResolver(), strMock);
		if (!TextUtils.isEmpty(strMock) && !strMock.equals("0")) {
			/*
			 * 系统设置中的允许MOCK选项已勾选
			 */
			return true;
		}
		return false;
	}
	private Thread thread = new Thread() {
		public void run() {
			while (true) {
				if (latitude == 0 || longitude == 0) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {

						// TODO Auto-generated catch block
						e.printStackTrace();

					}
					continue;
				}
				if(!isMockOpen()){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {

						// TODO Auto-generated catch block
						e.printStackTrace();

					}
					continue;
				}
				
				LocationManager locmanag = (LocationManager) EmulatorService.this
						.getSystemService(Context.LOCATION_SERVICE);
				String mock = LocationManager.GPS_PROVIDER;

				locmanag.addTestProvider(mock, false, true, false, false,
						false, false, false, 0, 5);
				locmanag.setTestProviderEnabled(mock, true);
				Location loc = new Location(mock);
				loc.setTime(System.currentTimeMillis());

				// 下面这两句很关键
				loc.setLatitude(latitude);
				loc.setLongitude(longitude);

				loc.setAccuracy(Criteria.ACCURACY_FINE);// 精确�?
				loc.setSpeed(50);
				if (android.os.Build.VERSION.SDK_INT >= 17) {
					loc.setElapsedRealtimeNanos(SystemClock
							.elapsedRealtimeNanos());// 实时运行时间
				}
				locmanag.setTestProviderStatus(mock,
						LocationProvider.AVAILABLE, null,
						System.currentTimeMillis());
				locmanag.setTestProviderLocation(mock, loc);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
 
					e.printStackTrace();

				}
			}

		}
	};

	/**   
	 */
	@Override
	public IBinder onBind(Intent intent) {
 
		thread.start();
		// TODO Auto-generated method stub
		return new EmulatorBinder();
	}

	public class EmulatorBinder extends Binder {

		public void setEmulatorLocation(double latitude, double longitude) {
			setLocation(latitude, longitude);
		}
	}

	private void setLocation(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;

	}

}
