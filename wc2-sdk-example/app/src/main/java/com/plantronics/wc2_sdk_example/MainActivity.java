package com.plantronics.wc2_sdk_example;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.plantronics.wc2sdk.*;
import com.plantronics.wc2sdk.configuration.OrientationConfiguration;
import com.plantronics.wc2sdk.snapshot.AccelerationSnapshot;
import com.plantronics.wc2sdk.snapshot.AngularVelocitySnapshot;
import com.plantronics.wc2sdk.snapshot.CompassHeadingSnapshot;
import com.plantronics.wc2sdk.snapshot.FreeFallSnapshot;
import com.plantronics.wc2sdk.snapshot.MagneticFieldSnapshot;
import com.plantronics.wc2sdk.snapshot.OrientationSnapshot;
import com.plantronics.wc2sdk.snapshot.ProximitySnapshot;
import com.plantronics.wc2sdk.snapshot.Snapshot;
import com.plantronics.wc2sdk.snapshot.StepCountSnapshot;
import com.plantronics.wc2sdk.snapshot.TapsSnapshot;
import com.plantronics.wc2sdk.snapshot.VoiceEventSnapshot;
import com.plantronics.wc2sdk.snapshot.WearingStateSnapshot;
import com.plantronics.wc2sdk.snapshot.datatypes.EulerAngles;
import com.plantronics.wc2sdk.snapshot.datatypes.Quaternion;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PairingListener, ConnectionListener, SnapshotListener {

	private static final String TAG = "com.plantronics.wc2_sdk_example.MainActivity";

	private Context 		_context;
	private Device 			_device;

	private ProgressBar		_headingProgressBar;
	private ProgressBar		_pitchProgressBar;
	private ProgressBar		_rollProgressBar;
	private Button			_zeroOrientationButton;
	private TextView		_headingValueTextView;
	private TextView		_pitchValueTextView;
	private TextView		_rollValueTextView;
	private TextView		_wearingStateValueTextView;
	private TextView		_localProximityValueTextView;
	private TextView		_tapsValueTextView;
	private TextView		_stepCountValueTextView;
	private TextView		_freeFallValueTextView;
	private TextView		_compassHeadingValueTextView;
	private TextView		_accelerationValueTextView;
	private TextView		_angularVelocityValueTextView;
	private TextView		_magneticFieldValueTextView;
	private TextView		_voiceEventValueTextView;


	/* ****************************************************************************************************
		 Activity
	*******************************************************************************************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		_context = this;

		_headingProgressBar = ((ProgressBar)findViewById(R.id.headingProgressBar));
		_pitchProgressBar = ((ProgressBar)findViewById(R.id.pitchProgressBar));
		_rollProgressBar = ((ProgressBar)findViewById(R.id.rollProgressBar));
		_headingValueTextView = ((TextView)findViewById(R.id.headingValueTextView));
		_pitchValueTextView = ((TextView)findViewById(R.id.pitchValueTextView));
		_rollValueTextView = ((TextView)findViewById(R.id.rollValueTextView));
		_zeroOrientationButton = ((Button)findViewById(R.id.zeroOrientationButton));
		_zeroOrientationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				zeroOrientation();
			}
		});
		_wearingStateValueTextView = ((TextView)findViewById(R.id.wearingStateValueTextView));
		_localProximityValueTextView = ((TextView)findViewById(R.id.localProximityValueTextView));
		_tapsValueTextView = ((TextView)findViewById(R.id.tapsValueTextView));
		_stepCountValueTextView = ((TextView)findViewById(R.id.stepCountValueTextView));
		_freeFallValueTextView = ((TextView)findViewById(R.id.freeFallValueTextView));
		_compassHeadingValueTextView = ((TextView)findViewById(R.id.compassHeadingValueTextView));
		_accelerationValueTextView = ((TextView)findViewById(R.id.accelerationValueTextView));
		_angularVelocityValueTextView = ((TextView)findViewById(R.id.angularVelocityValueTextView));
		_magneticFieldValueTextView = ((TextView)findViewById(R.id.magneticFieldValueTextView));
		_voiceEventValueTextView = ((TextView)findViewById(R.id.voiceEventValueTextView));

		if (!Device.getIsInitialized()) {
			Log.v(TAG, "Initializing wc2-sdk...");
			try {
				Device.initialize(this, new Device.InitializationCallback() {
					@Override
					public void onInitialized() {
						Log.i(TAG, "onInitialized()");
					Device.registerPairingListener((PairingListener)_context);

						tryConnect();
					}

					@Override
					public void onFailure(int error) {
						Log.i(TAG, "onFailure()");

					if (error == Device.ERROR_PLTHUB_NOT_AVAILABLE) {
						Log.i(TAG, "PLTHub was not found.");
					}
					else if (error == Device.ERROR_FAILED_GET_DEVICE_LIST) {
						Log.i(TAG, "Failed to get device list.");
					}
					}
				});
			}
			catch (Exception e) {
				Log.e(TAG, "Exception initializing wc2-sdk: " + e);
			}
		}
	}

	@Override
	public void onResume() {
		Log.i(TAG, "onResume()");
		super.onResume();

		_context = this;

		if (_device != null) {
			_device.onResume();
		}
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "onPause()");
		super.onPause();

		_context = null;

		if (_device != null) {
			_device.onPause();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/* ****************************************************************************************************
			 Private
	*******************************************************************************************************/

	private void tryConnect() {
		ArrayList<Device> devices = Device.getPairedDevices();
		Log.i(TAG, "devices: " + devices);

		try {
			if (devices.size() > 0) {
				// NOTE: because of Android OS bugs and limitations, it is recommended that you explicitly check the MAC address
				// of each device returned by Device.getPairedDevices(), and only attempt to connect to the one you're interested in.
				// For example:
				// String myDeviceMAC = "00:11:22:33:44:55";
				// if (devices.get(0).getAddress() == myDeviceMAC) {
				//  	use this device...
				// }

				_device = devices.get(0);
				_device.registerConnectionListener((ConnectionListener)_context);
				_device.openConnection();
			}
			else {
				Log.i(TAG, "No paired PLT devices found!");
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Exception opening connection: " + e);
		}
	}

	private void zeroOrientation() {
		// "zero" orientation to current position
		try {
			OrientationConfiguration orientationConfiguration = new OrientationConfiguration(Quaternion.ZeroQuaternion(), OrientationConfiguration.ORIENTATION_REFERENCE_TYPE_BODY);
			_device.setConfiguration(orientationConfiguration, Device.SERVICE_ORIENTATION);
		}
		catch (Exception e) {
			Log.e(TAG, "Exception calibrating orientation: " + e);
		}
	}

	/* ****************************************************************************************************
			 PairingListener
	*******************************************************************************************************/

	public void onDevicePaired(Device device) {
		Log.i(TAG, "onDevicePaired(): " + device);

		tryConnect();
	}

	public void onDeviceUnpaired(Device device) {
		Log.i(TAG, "onDeviceUnpaired(): " + device);
	}

	/* ****************************************************************************************************
			 ConnectionListener
	*******************************************************************************************************/

	public void onConnectionOpen(Device device) {
		Log.i(TAG, "onConnectionOpen()");

		// subscribe to all services
		try {
//			_device.subscribe(this, Device.SERVICE_ORIENTATION, (short)300);
//			_device.subscribe(this, Device.SERVICE_WEARING_STATE, (short)300);
//			_device.subscribe(this, Device.SERVICE_PROXIMITY, (short)300);
//			_device.subscribe(this, Device.SERVICE_TAPS, (short)300);
//			_device.subscribe(this, Device.SERVICE_STEP_COUNT, (short)300);
//			_device.subscribe(this, Device.SERVICE_FREE_FALL, (short)300);
			_device.subscribe(this, Device.SERVICE_COMPASS_HEADING, (short)300);
//			_device.subscribe(this, Device.SERVICE_ACCELERATION, (short)300);
//			_device.subscribe(this, Device.SERVICE_ANGULAR_VELOCITY, (short)300);
//			_device.subscribe(this, Device.SERVICE_MAGNETIC_FIELD, (short)300);
//			_device.subscribe(this, Device.SERVICE_VOICE_EVENTS, (short)300);
		}
		catch (Exception e) {
			Log.e(TAG, "Exception subscribing to services: " + e);
		}

		//zeroOrientation();
	}

	public void onConnectionFailedToOpen(Device device, int error) {
		Log.i(TAG, "onConnectionFailedToOpen()");

		if (error == Device.ERROR_CONNECTION_TIMEOUT) {
			Log.i(TAG, "Open connection timed out.");
		}
	}

	public void onConnectionClosed(Device device) {
		Log.i(TAG, "onConnectionClosed()");

		_device = null;
	}

	/* ****************************************************************************************************
			 InfoListener
	*******************************************************************************************************/

	public void onSubscriptionChanged(Subscription oldSubscription, Subscription newSubscription) {
		Log.i(TAG, "onSubscriptionChanged(): oldSubscription=" + oldSubscription + ", newSubscription=" + newSubscription);
	}

	public void onSnapshotReceived(final Snapshot snapshot) {
		Log.i(TAG, "onSnapshotReceived(): " + snapshot);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (snapshot.getClass() == OrientationSnapshot.class) {
					OrientationSnapshot snap = (OrientationSnapshot)snapshot;
					EulerAngles eulerAngles = snap.getEulerAngles();

					int heading = (int)Math.round(eulerAngles.getX());
					int pitch = (int)Math.round(eulerAngles.getY());
					int roll = (int)Math.round(eulerAngles.getZ());

					_headingProgressBar.setProgress(heading + 180);
					_pitchProgressBar.setProgress(pitch + 90);
					_rollProgressBar.setProgress(roll + 180);

					// heading uses the "right-hand" rule. http://en.wikipedia.org/wiki/Right-hand_rule
					// most people find it more intuitive if the angle increases when rotated in the opposite direction
					_headingValueTextView.setText(heading + "°");
					_pitchValueTextView.setText(pitch + "°");
					_rollValueTextView.setText(roll + "°");

				}
				else if (snapshot.getClass() == WearingStateSnapshot.class) {
					WearingStateSnapshot snap = (WearingStateSnapshot)snapshot;
					_wearingStateValueTextView.setText((snap.isBeingWorn() ? "yes" : "no"));
				}
				else if (snapshot.getClass() == ProximitySnapshot.class) {
					ProximitySnapshot snap = (ProximitySnapshot)snapshot;
					_localProximityValueTextView.setText(ProximitySnapshot.getStringForProximity(snap.getLocalProximity()));
				}
				else if (snapshot.getClass() == TapsSnapshot.class) {
					TapsSnapshot snap = (TapsSnapshot)snapshot;
					int count = snap.getCount();
					String tapss = (count > 1 ? " taps" : " tap");
					_tapsValueTextView.setText((count == 0 ? "-" : count + tapss + " in " + TapsSnapshot.getStringForTapDirection(snap.getDirection())));
				}
				else if (snapshot.getClass() == StepCountSnapshot.class) {
					StepCountSnapshot snap = (StepCountSnapshot)snapshot;
					_stepCountValueTextView.setText(snap.getSteps() + " steps");
				}
				else if (snapshot.getClass() == FreeFallSnapshot.class) {
					FreeFallSnapshot snap = (FreeFallSnapshot)snapshot;
					_freeFallValueTextView.setText((snap.isInFreeFall() ? "yes" : "no"));
				}
				else if (snapshot.getClass() ==CompassHeadingSnapshot.class) {
					CompassHeadingSnapshot snap = (CompassHeadingSnapshot)snapshot;
					_compassHeadingValueTextView.setText((int)Math.round(snap.getHeading()) + "°");
				}
				else if (snapshot.getClass() == AccelerationSnapshot.class) {
					AccelerationSnapshot snap = (AccelerationSnapshot)snapshot;
					String xStr = String.format("%.2f", snap.getAcceleration().getX());
					String yStr = String.format("%.2f", snap.getAcceleration().getY());
					String zStr = String.format("%.2f", snap.getAcceleration().getZ());
					_accelerationValueTextView.setText("{" + xStr + ", " + yStr + ", " + zStr + "}");
				}
				else if (snapshot.getClass() == AngularVelocitySnapshot.class) {
					AngularVelocitySnapshot snap = (AngularVelocitySnapshot)snapshot;
					String xStr = String.format("%.2f", snap.getAngularVelocity().getX());
					String yStr = String.format("%.2f", snap.getAngularVelocity().getY());
					String zStr = String.format("%.2f", snap.getAngularVelocity().getZ());
					_angularVelocityValueTextView.setText("{" + xStr + ", " + yStr + ", " + zStr + "}");
				}
				else if (snapshot.getClass() == MagneticFieldSnapshot.class) {
					MagneticFieldSnapshot snap = (MagneticFieldSnapshot)snapshot;
					String xStr = String.format("%.2f", snap.getMagneticField().getX());
					String yStr = String.format("%.2f", snap.getMagneticField().getY());
					String zStr = String.format("%.2f", snap.getMagneticField().getZ());
					_magneticFieldValueTextView.setText("{" + xStr + ", " + yStr + ", " + zStr + "}");
				}
				else if (snapshot.getClass() == VoiceEventSnapshot.class) {
					VoiceEventSnapshot snap = (VoiceEventSnapshot)snapshot;
					_voiceEventValueTextView.setText(VoiceEventSnapshot.getStringForPhrase(snap.getPhrase()));
				}
			}
		});
	}
}
