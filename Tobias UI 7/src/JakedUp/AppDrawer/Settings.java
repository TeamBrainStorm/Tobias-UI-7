package JakedUp.AppDrawer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;


import Yuku.AmbilWarna.AmbilWarnaDialog;
import Yuku.AmbilWarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.d4a.tobias.R;
import java.util.Calendar;

public class Settings extends PreferenceActivity {

    public static final int RESULT_NEED_RESTART_AND_CLEAR_DB = 1;
    public static final int RESULT_NEED_RESTART = 2;
    public static final int RESULT_IMAGE_SELECT = 3;
    public static final int RESULT_UNHIDE_APP = 4;
    public static final int SORT_BY_NAME = 0;
    public static final int SORT_BY_INSTALLATION_DATE = 1;
    public static final int SORT_BY_LATEST_INSTALLED_FIRST = 2;
    public static final int SORT_BY_MOST_USED = 3;
    public static int bg_color, bg_type, font_color, font_size, icon_size, list_style, row_spacing, num_columns_v, num_columns_h, num_rows_v, num_rows_h,
            home_position, screen_orientation, bg_alpha, sortBy;
    public static String font, bg_image;
    public static boolean show_home_button, fullscreen, remember_position, low_quality, show_indicators, autohide_indicators, service, sort_folders_first;
    private Context context = this;
    private int result = RESULT_CANCELED;
    private AlertDialog fontDialog;
    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;
    private Intent intentResult;
    ListPreference pref_list_style, pref_bg_type, pref_home_position, pref_screen_orientation;
    Preference pref_bg_color, pref_bg_image, pref_font_color, pref_font_size, pref_icon_size, pref_show_home, pref_fullscreen, pref_restore_default, pref_row_spacing, pref_num_columns, pref_bg_alpha,
            pref_hidden_apps, pref_remember_position, pref_num_rows, pref_low_quality, pref_show_indicators,
            pref_autohide_indicators, pref_font, pref_check_version, pref_export, pref_service;
    PreferenceScreen pref_import, pref_backups;
    public static final String PREFS_FILENAME = "AppDrawer_Prefs";
    public static final String APPDRAWER_DIR = "/AppDrawer/";
    public static final String FONTS_DIR = APPDRAWER_DIR + "fonts/";
    public static final String BACKUPS_DIR = APPDRAWER_DIR + "backups/";
    public static final String BG_IMG_FILE = APPDRAWER_DIR + "background.png";

    public static void getPrefs(SharedPreferences spref) {

        sp = spref;
        editor = sp.edit();

        bg_color = sp.getInt("pref_bg_color", 0xff000000);
        bg_type = sp.getInt("pref_bg_type", 0);
        bg_image = sp.getString("pref_bg_image", "");
        font_color = sp.getInt("pref_font_color", 0xffffffff);
        font_size = sp.getInt("pref_font_size", 22);
        icon_size = sp.getInt("pref_icon_size", 50);
        show_home_button = sp.getBoolean("pref_show_home", true);
        fullscreen = sp.getBoolean("pref_fullscreen", true);
        list_style = sp.getInt("pref_list_style", 0);
        row_spacing = sp.getInt("pref_row_spacing", 10);
        num_columns_h = sp.getInt("pref_num_columns_h", 6);
        num_columns_v = sp.getInt("pref_num_columns_v", 4);
        num_rows_h = sp.getInt("pref_num_rows_h", 3);
        num_rows_v = sp.getInt("pref_num_rows_v", 4);
        home_position = sp.getInt("pref_home_position", 0);
        screen_orientation = sp.getInt("pref_screen_orientation", 0);
        bg_alpha = sp.getInt("pref_bg_alpha", 150);
        remember_position = sp.getBoolean("pref_remember_position", true);
        low_quality = sp.getBoolean("pref_low_quality", false);
        show_indicators = sp.getBoolean("pref_show_indicators", true);
        autohide_indicators = sp.getBoolean("pref_autohide_indicators", true);
        font = sp.getString("pref_font", "Default");
        service = sp.getBoolean("pref_service", false);
        sort_folders_first = sp.getBoolean("sort_folders_first", true);
        sortBy = sp.getInt("sortBy", SORT_BY_NAME);

        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File fontsFolder = new File(sdCard.getAbsolutePath() + FONTS_DIR);
            fontsFolder.mkdirs();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);


        getPrefs(getSharedPreferences(PREFS_FILENAME, Activity.MODE_PRIVATE));


        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            findPreference("pref_app_version").setTitle(pi.applicationInfo.loadLabel(getPackageManager()) + " " + pi.versionName);
            findPreference("pref_app_version").setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final String[] latestVersion;
                    if ((latestVersion = checkNewVersion()) != null) {

                        AlertDialog.Builder adb = new Builder(context);

                        adb.setCancelable(true).setNegativeButton(R.string.button_cancel, null).setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent downloadIntent = new Intent("android.intent.action.VIEW", Uri.parse(latestVersion[1]));
                                startActivity(downloadIntent);
                            }
                        }).setMessage(getResources().getString(R.string.new_version) + " " + latestVersion[0] + "?");

                        adb.create().show();

                    } else {
                        Toast.makeText(getApplicationContext(), R.string.no_new_version,
                                Toast.LENGTH_LONG).show();
                    }

                    return false;
                }
            });

        } catch (NameNotFoundException e1) {

            e1.printStackTrace();
        }



        pref_autohide_indicators = findPreference("pref_autohide_indicators");
        ((CheckBoxPreference) pref_autohide_indicators).setChecked(autohide_indicators);
        pref_autohide_indicators.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                CheckBoxPreference cbp = (CheckBoxPreference) preference;
                cbp.setChecked(!cbp.isChecked());
                return false;
            }
        });

        pref_autohide_indicators.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                boolean b = Boolean.parseBoolean(newValue.toString());

                autohide_indicators = b;

                editor.putBoolean("pref_autohide_indicators", autohide_indicators);

                editor.commit();

                if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                    result = RESULT_OK;
                }

                return false;
            }
        });




        pref_show_indicators = findPreference("pref_show_indicators");
        ((CheckBoxPreference) pref_show_indicators).setChecked(show_indicators);
        pref_show_indicators.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                CheckBoxPreference cbp = (CheckBoxPreference) preference;
                cbp.setChecked(!cbp.isChecked());
                return false;
            }
        });

        pref_show_indicators.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                boolean b = Boolean.parseBoolean(newValue.toString());

                show_indicators = b;

                editor.putBoolean("pref_show_indicators", show_indicators);

                if (!show_indicators) {
                    pref_autohide_indicators.setEnabled(false);
                } else {
                    pref_autohide_indicators.setEnabled(true);
                }

                editor.commit();

                if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                    result = RESULT_OK;
                }

                return false;
            }
        });

        if (!show_indicators) {
            pref_autohide_indicators.setEnabled(false);
        } else {
            pref_autohide_indicators.setEnabled(true);
        }




        pref_list_style = (ListPreference) findPreference("pref_list_style");

        pref_num_columns = findPreference("pref_num_columns");

        pref_num_rows = findPreference("pref_num_rows");

        pref_list_style.setValueIndex(list_style);

        pref_list_style.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {

                list_style = Integer.parseInt((String) newValue);

                if (list_style == 0) {
                    font_size = 22;
                    pref_num_columns.setEnabled(false);
                } else if (list_style == 1 || list_style == 2) {
                    font_size = 12;
                    pref_num_columns.setEnabled(true);
                }
                if (list_style == 2) {
                    pref_num_rows.setEnabled(true);
                    pref_show_indicators.setEnabled(true);
                    if (!show_indicators) {
                        pref_autohide_indicators.setEnabled(false);
                    } else {
                        pref_autohide_indicators.setEnabled(true);
                    }
                } else {
                    pref_num_rows.setEnabled(false);
                    pref_show_indicators.setEnabled(false);
                    pref_autohide_indicators.setEnabled(false);

                }

                editor.putInt("pref_list_style", list_style);
                editor.putInt("pref_font_size", font_size);

                editor.commit();

                if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                    result = RESULT_OK;
                }

                pref_list_style.setSummary(pref_list_style.getEntries()[list_style]);


                pref_list_style.setValueIndex(list_style);


                pref_font_size.setSummary(String.valueOf(font_size));



                return false;
            }
        });

        if (list_style == 0) {
            //font_size = 22;
            pref_num_columns.setEnabled(false);
        } else if (list_style == 1 || list_style == 2) {
            //font_size = 12;
            pref_num_columns.setEnabled(true);
        }
        if (list_style == 2) {
            pref_num_rows.setEnabled(true);
            pref_show_indicators.setEnabled(true);
            if (!show_indicators) {
                pref_autohide_indicators.setEnabled(false);
            } else {
                pref_autohide_indicators.setEnabled(true);
            }
        } else {
            pref_num_rows.setEnabled(false);
            pref_show_indicators.setEnabled(false);
            pref_autohide_indicators.setEnabled(false);

        }


        pref_list_style.setSummary(pref_list_style.getEntries()[list_style]);






        pref_screen_orientation = (ListPreference) findPreference("pref_screen_orientation");

        pref_screen_orientation.setValueIndex(screen_orientation);

        pref_screen_orientation.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {

                screen_orientation = Integer.parseInt((String) newValue);

                editor.putInt("pref_screen_orientation", screen_orientation);

                editor.commit();

                result = RESULT_NEED_RESTART;

                pref_screen_orientation.setSummary(pref_screen_orientation.getEntries()[screen_orientation]);


                pref_screen_orientation.setValueIndex(screen_orientation);

                return false;
            }
        });


        pref_screen_orientation.setSummary(pref_screen_orientation.getEntries()[screen_orientation]);



        pref_row_spacing = findPreference("pref_row_spacing");
        pref_row_spacing.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {


                LayoutInflater inflater = LayoutInflater.from(context);
                View seekbarView = inflater.inflate(R.layout.seekbar_chooser_dialog, null);

                final AlertDialog.Builder db = new Builder(context);

                final SeekBar sb = (SeekBar) seekbarView.findViewById(R.id.seekBarSize);

                final TextView tv = (TextView) seekbarView.findViewById(R.id.seekbarTextView);

                db.setTitle(R.string.pref_row_spacing_title).setCancelable(true).setView(seekbarView).setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        row_spacing = sb.getProgress();

                        editor.putInt("pref_row_spacing", row_spacing);

                        editor.commit();

                        if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                            result = RESULT_OK;
                        }

                        pref_row_spacing.setSummary(String.valueOf(row_spacing));

                        return;
                    }
                }).setNegativeButton(R.string.button_cancel, null);


                AlertDialog d = db.create();




                Button bl = (Button) seekbarView.findViewById(R.id.button_less);

                Button bm = (Button) seekbarView.findViewById(R.id.button_more);

                bl.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        sb.setProgress(sb.getProgress() - 1);
                        tv.setText(getString(R.string.pref_row_spacing) + ": " + sb.getProgress());
                    }
                });

                bm.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        sb.setProgress(sb.getProgress() + 1);
                        tv.setText(getString(R.string.pref_row_spacing) + ": " + sb.getProgress());
                    }
                });

                tv.setText(getString(R.string.pref_row_spacing) + ": " + row_spacing);


                sb.setProgress(row_spacing);
                sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {

                        tv.setText(getString(R.string.pref_row_spacing) + ": " + arg1);
                    }
                });

                d.show();

                return false;
            }
        });


        pref_row_spacing.setSummary(String.valueOf(row_spacing));








        pref_num_columns.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {


                LayoutInflater inflater = LayoutInflater.from(context);
                View seekbarView = inflater.inflate(R.layout.double_seekbar_chooser_dialog, null);

                final AlertDialog.Builder db = new Builder(context);

                final SeekBar sb = (SeekBar) seekbarView.findViewById(R.id.seekBarSize);

                final TextView tv = (TextView) seekbarView.findViewById(R.id.seekbarTextView);

                final SeekBar sb2 = (SeekBar) seekbarView.findViewById(R.id.seekBarSize2);

                final TextView tv2 = (TextView) seekbarView.findViewById(R.id.seekbarTextView2);


                db.setTitle(R.string.pref_num_columns_title).setCancelable(true).setView(seekbarView).setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        num_columns_v = sb.getProgress() + 1;

                        num_columns_h = sb2.getProgress() + 1;

                        editor.putInt("pref_num_columns_v", num_columns_v);

                        editor.putInt("pref_num_columns_h", num_columns_h);

                        editor.commit();

                        if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                            result = RESULT_OK;
                        }

                        pref_num_columns.setSummary(getResources().getString(R.string.portrait) + ": " + String.valueOf(num_columns_v) + " | " + getResources().getString(R.string.landscape) + ": " + String.valueOf(num_columns_h));

                        return;
                    }
                }).setNegativeButton(R.string.button_cancel, null);


                AlertDialog d = db.create();



                Button bl = (Button) seekbarView.findViewById(R.id.button_less);

                Button bm = (Button) seekbarView.findViewById(R.id.button_more);

                bl.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        sb.setProgress(sb.getProgress() - 1);
                        tv.setText(getString(R.string.pref_num_columns) + " " + getResources().getString(R.string.portrait) + ": " + (sb.getProgress() + 1));
                    }
                });

                bm.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        sb.setProgress(sb.getProgress() + 1);
                        tv.setText(getString(R.string.pref_num_columns) + " " + getResources().getString(R.string.portrait) + ": " + (sb.getProgress() + 1));
                    }
                });


                sb.setMax(9);
                tv.setText(getString(R.string.pref_num_columns) + " " + getResources().getString(R.string.portrait) + ": " + num_columns_v);


                sb.setProgress(num_columns_v - 1);
                sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {

                        tv.setText(getString(R.string.pref_num_columns) + " " + getResources().getString(R.string.portrait) + ": " + (arg1 + 1));
                    }
                });


                Button bl2 = (Button) seekbarView.findViewById(R.id.button_less2);

                Button bm2 = (Button) seekbarView.findViewById(R.id.button_more2);

                bl2.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        sb2.setProgress(sb2.getProgress() - 1);
                        tv2.setText(getString(R.string.pref_num_columns) + " " + getResources().getString(R.string.landscape) + ": " + (sb2.getProgress() + 1));
                    }
                });

                bm2.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        sb2.setProgress(sb2.getProgress() + 1);
                        tv2.setText(getString(R.string.pref_num_columns) + " " + getResources().getString(R.string.landscape) + ": " + (sb2.getProgress() + 1));
                    }
                });


                sb2.setMax(9);

                tv2.setText(getString(R.string.pref_num_columns) + " " + getResources().getString(R.string.landscape) + ": " + num_columns_h);


                sb2.setProgress(num_columns_h - 1);
                sb2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {

                        tv2.setText(getString(R.string.pref_num_columns) + " " + getResources().getString(R.string.landscape) + ": " + (arg1 + 1));
                    }
                });


                d.show();

                return false;
            }
        });


        pref_num_columns.setSummary(getResources().getString(R.string.portrait) + ": " + String.valueOf(num_columns_v) + " | " + getResources().getString(R.string.landscape) + ": " + String.valueOf(num_columns_h));





        pref_num_rows.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {


                LayoutInflater inflater = LayoutInflater.from(context);
                View seekbarView = inflater.inflate(R.layout.double_seekbar_chooser_dialog, null);

                final AlertDialog.Builder db = new Builder(context);

                final SeekBar sb = (SeekBar) seekbarView.findViewById(R.id.seekBarSize);

                final TextView tv = (TextView) seekbarView.findViewById(R.id.seekbarTextView);

                final SeekBar sb2 = (SeekBar) seekbarView.findViewById(R.id.seekBarSize2);

                final TextView tv2 = (TextView) seekbarView.findViewById(R.id.seekbarTextView2);

                db.setTitle(R.string.pref_num_rows_title).setCancelable(true).setView(seekbarView).setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        num_rows_v = sb.getProgress() + 1;

                        num_rows_h = sb2.getProgress() + 1;

                        editor.putInt("pref_num_rows_v", num_rows_v);

                        editor.putInt("pref_num_rows_h", num_rows_h);

                        editor.commit();

                        if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                            result = RESULT_OK;
                        }

                        pref_num_rows.setSummary(getResources().getString(R.string.portrait) + ": " + String.valueOf(num_rows_v) + " | " + getResources().getString(R.string.landscape) + ": " + String.valueOf(num_rows_h));

                        return;
                    }
                }).setNegativeButton(R.string.button_cancel, null);


                AlertDialog d = db.create();

                Button bl = (Button) seekbarView.findViewById(R.id.button_less);

                Button bm = (Button) seekbarView.findViewById(R.id.button_more);

                bl.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        sb.setProgress(sb.getProgress() - 1);
                        tv.setText(getString(R.string.pref_num_rows) + " " + getResources().getString(R.string.portrait) + ": " + (sb.getProgress() + 1));
                    }
                });

                bm.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        sb.setProgress(sb.getProgress() + 1);
                        tv.setText(getString(R.string.pref_num_rows) + " " + getResources().getString(R.string.portrait) + ": " + (sb.getProgress() + 1));
                    }
                });



                sb.setMax(9);
                tv.setText(getString(R.string.pref_num_rows) + " " + getResources().getString(R.string.portrait) + ": " + num_rows_v);


                sb.setProgress(num_rows_v - 1);
                sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {

                        tv.setText(getString(R.string.pref_num_rows) + " " + getResources().getString(R.string.portrait) + ": " + (arg1 + 1));
                    }
                });



                Button bl2 = (Button) seekbarView.findViewById(R.id.button_less2);

                Button bm2 = (Button) seekbarView.findViewById(R.id.button_more2);

                bl2.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        sb2.setProgress(sb2.getProgress() - 1);
                        tv2.setText(getString(R.string.pref_num_rows) + " " + getResources().getString(R.string.landscape) + ": " + (sb2.getProgress() + 1));
                    }
                });

                bm2.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        sb2.setProgress(sb2.getProgress() + 1);
                        tv2.setText(getString(R.string.pref_num_rows) + " " + getResources().getString(R.string.landscape) + ": " + (sb2.getProgress() + 1));
                    }
                });

                sb2.setMax(9);

                tv2.setText(getString(R.string.pref_num_rows) + " " + getResources().getString(R.string.landscape) + ": " + num_rows_h);


                sb2.setProgress(num_rows_h - 1);
                sb2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {

                        tv2.setText(getString(R.string.pref_num_rows) + " " + getResources().getString(R.string.landscape) + ": " + (arg1 + 1));
                    }
                });


                d.show();

                return false;
            }
        });


        pref_num_rows.setSummary(getResources().getString(R.string.portrait) + ": " + String.valueOf(num_rows_v) + " | " + getResources().getString(R.string.landscape) + ": " + String.valueOf(num_rows_h));




        pref_bg_alpha = findPreference("pref_bg_alpha");
        pref_bg_alpha.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                LayoutInflater inflater = LayoutInflater.from(context);
                View seekbarView = inflater.inflate(R.layout.seekbar_chooser_dialog, null);

                final AlertDialog.Builder db = new Builder(context);

                final SeekBar sb = (SeekBar) seekbarView.findViewById(R.id.seekBarSize);

                final TextView tv = (TextView) seekbarView.findViewById(R.id.seekbarTextView);

                db.setTitle(R.string.pref_bg_alpha).setCancelable(true).setView(seekbarView).setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        bg_alpha = sb.getProgress();

                        editor.putInt("pref_bg_alpha", bg_alpha);

                        editor.commit();

                        if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                            result = RESULT_OK;
                        }

                        pref_bg_alpha.setSummary(String.valueOf(bg_alpha));

                        return;
                    }
                }).setNegativeButton(R.string.button_cancel, null);


                AlertDialog d = db.create();



                Button bl = (Button) seekbarView.findViewById(R.id.button_less);

                Button bm = (Button) seekbarView.findViewById(R.id.button_more);

                bl.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        sb.setProgress(sb.getProgress() - 1);
                        tv.setText(getString(R.string.pref_bg_alpha) + ": " + sb.getProgress());
                    }
                });

                bm.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        sb.setProgress(sb.getProgress() + 1);
                        tv.setText(getString(R.string.pref_bg_alpha) + ": " + sb.getProgress());
                    }
                });

                tv.setText(getString(R.string.pref_bg_alpha) + ": " + bg_alpha);

                sb.setMax(255);
                sb.setProgress(bg_alpha);
                sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {

                        tv.setText(getString(R.string.pref_bg_alpha) + ": " + arg1);

                    }
                });

                d.show();

                return false;
            }
        });

        pref_bg_alpha.setSummary(String.valueOf(bg_alpha));





        pref_bg_type = (ListPreference) findPreference("pref_bg_type");
        pref_bg_color = findPreference("pref_bg_color");
        pref_bg_image = findPreference("pref_bg_image");
        pref_low_quality = findPreference("pref_low_quality");

        pref_bg_type.setValueIndex(bg_type);

        pref_bg_type.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                bg_type = Integer.parseInt((String) newValue);

                editor.putInt("pref_bg_type", bg_type);

                editor.commit();

                result = RESULT_NEED_RESTART;

                pref_bg_type.setSummary(pref_bg_type.getEntries()[bg_type]);

                if (bg_type == 0) {
                    pref_bg_color.setEnabled(true);
                    pref_bg_image.setEnabled(false);
                    pref_bg_alpha.setEnabled(false);
                    pref_low_quality.setEnabled(false);
                } else if (bg_type == 1) {
                    pref_bg_color.setEnabled(false);
                    pref_bg_image.setEnabled(true);
                    pref_bg_alpha.setEnabled(true);
                    pref_low_quality.setEnabled(true);
                } else {
                    pref_bg_color.setEnabled(false);
                    pref_bg_image.setEnabled(false);
                    pref_bg_alpha.setEnabled(true);
                    pref_low_quality.setEnabled(false);
                }

                pref_bg_type.setValueIndex(bg_type);


                return false;
            }
        });


        pref_bg_type.setSummary(pref_bg_type.getEntries()[bg_type]);


        if (bg_type == 0) {
            pref_bg_color.setEnabled(true);
            pref_bg_image.setEnabled(false);
            pref_bg_alpha.setEnabled(false);
            pref_low_quality.setEnabled(false);
            //Color solido

        } else if (bg_type == 1) {
            pref_bg_color.setEnabled(false);
            pref_bg_image.setEnabled(true);
            pref_bg_alpha.setEnabled(true);
            pref_low_quality.setEnabled(true);
            //Imagen como fondo

        } else {
            pref_bg_color.setEnabled(false);
            pref_bg_image.setEnabled(false);
            pref_bg_alpha.setEnabled(true);
            pref_low_quality.setEnabled(false);
            //Wallpaper
        }


        pref_bg_color.setSummary("#" + Integer.toHexString(bg_color).substring(2));


        pref_bg_color.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {


                AmbilWarnaDialog awd = new AmbilWarnaDialog(context, bg_color, new OnAmbilWarnaListener() {

                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        bg_color = color;

                        editor.putInt("pref_bg_color", bg_color);

                        editor.commit();

                        if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                            result = RESULT_OK;
                        }


                        pref_bg_color.setSummary("#" + Integer.toHexString(bg_color).substring(2));



                    }

                    public void onCancel(AmbilWarnaDialog dialog) {
                        //
                    }
                });

                awd.show();

                return false;
            }
        });



        pref_bg_image.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {


                Intent photoPickerIntent;
                String status = Environment.getExternalStorageState();
                if (status.equals(Environment.MEDIA_MOUNTED)) {
                    photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                } else {
                    photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                }



                File file = new File(Environment.getExternalStorageDirectory(), BG_IMG_FILE);
                try {
                    file.createNewFile();
                } catch (IOException e) {
                }

                photoPickerIntent.setType("image/*");
                photoPickerIntent.putExtra("crop", "true");
                photoPickerIntent.putExtra("aspectX", 2);
                photoPickerIntent.putExtra("aspectY", 3);
                photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
                startActivityForResult(photoPickerIntent, RESULT_IMAGE_SELECT);


                if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                    result = RESULT_OK;
                }



                return false;
            }
        });



        ((CheckBoxPreference) pref_low_quality).setChecked(low_quality);
        pref_low_quality.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                CheckBoxPreference cbp = (CheckBoxPreference) preference;
                cbp.setChecked(!cbp.isChecked());
                return false;
            }
        });

        pref_low_quality.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                boolean b = Boolean.parseBoolean(newValue.toString());

                low_quality = b;

                editor.putBoolean("pref_low_quality", low_quality);

                editor.commit();

                if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                    result = RESULT_OK;
                }

                return false;
            }
        });





        pref_font_color = findPreference("pref_font_color");
        pref_font_color.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {


                AmbilWarnaDialog awd = new AmbilWarnaDialog(context, font_color, new OnAmbilWarnaListener() {

                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        font_color = color;

                        editor.putInt("pref_font_color", font_color);

                        editor.commit();

                        if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                            result = RESULT_OK;
                        }

                        pref_font_color.setSummary("#" + Integer.toHexString(font_color).substring(2));

                    }

                    public void onCancel(AmbilWarnaDialog dialog) {
                        //
                    }
                });

                awd.show();

                return false;
            }
        });


        pref_font_color.setSummary("#" + Integer.toHexString(font_color).substring(2));



        pref_font = findPreference("pref_font");
        pref_font.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {


                LayoutInflater inflater = LayoutInflater.from(context);
                View fontChooser = inflater.inflate(R.layout.font_chooser, null);

                ListView fontsList = (ListView) fontChooser.findViewById(R.id.fontsList);

                ArrayList<String> items = new ArrayList<String>();
                items.add("Default");
                try {
                    String[] installedFonts = context.getAssets().list("fonts");
                    for (int i = 0; i < installedFonts.length; i++) {
                        items.add("." + installedFonts[i]);
                    }

                    File sdCard = Environment.getExternalStorageDirectory();
                    File fontsFolder = new File(sdCard.getAbsolutePath() + FONTS_DIR);

                    String[] installedFontsByUser = fontsFolder.list();
                    if (installedFontsByUser.length == 0) {
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.no_fonts), Toast.LENGTH_LONG).show();
                    }
                    for (int i = 0; i < installedFontsByUser.length; i++) {
                        if (installedFontsByUser[i].endsWith(".ttf") && !installedFontsByUser[i].startsWith(".")) {
                            items.add("+" + installedFontsByUser[i]);
                        }
                    }

                } catch (Exception e) {

                    e.printStackTrace();
                }


                fontsList.setAdapter(new FontChooserAdapter(context, R.layout.font_item, items));
                fontsList.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                            int arg2, long arg3) {

                        //font = arg0.getItemAtPosition(arg2);
                        font = arg0.getItemAtPosition(arg2).toString();
                        editor.putString("pref_font", font);

                        editor.commit();

                        if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                            result = RESULT_OK;
                        }

                        pref_font.setSummary(font);

                        fontDialog.dismiss();

                    }
                });




                final AlertDialog.Builder db = new Builder(context);


                db.setTitle(R.string.pref_font).setCancelable(true).setView(fontChooser).setNegativeButton(R.string.button_cancel, null);


                fontDialog = db.create();



                fontDialog.show();

                return false;
            }
        });

        pref_font.setSummary(font);




        pref_font_size = findPreference("pref_font_size");
        pref_font_size.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                LayoutInflater inflater = LayoutInflater.from(context);
                View seekbarView = inflater.inflate(R.layout.seekbar_chooser_dialog, null);

                final AlertDialog.Builder db = new Builder(context);

                final SeekBar sb = (SeekBar) seekbarView.findViewById(R.id.seekBarSize);

                final TextView tv = (TextView) seekbarView.findViewById(R.id.seekbarTextView);

                db.setTitle(R.string.pref_font_size_title).setCancelable(true).setView(seekbarView).setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        font_size = sb.getProgress();

                        editor.putInt("pref_font_size", font_size);

                        editor.commit();

                        if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                            result = RESULT_OK;
                        }

                        pref_font_size.setSummary(String.valueOf(font_size));

                        return;
                    }
                }).setNegativeButton(R.string.button_cancel, null);


                AlertDialog d = db.create();



                Button bl = (Button) seekbarView.findViewById(R.id.button_less);

                Button bm = (Button) seekbarView.findViewById(R.id.button_more);

                bl.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        sb.setProgress(sb.getProgress() - 1);
                        tv.setText(getString(R.string.pref_font_size) + ": " + sb.getProgress());
                    }
                });

                bm.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        sb.setProgress(sb.getProgress() + 1);
                        tv.setText(getString(R.string.pref_font_size) + ": " + sb.getProgress());
                    }
                });

                tv.setText(getString(R.string.pref_font_size) + ": " + font_size);


                sb.setProgress(font_size);
                sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {

                        tv.setText(getString(R.string.pref_font_size) + ": " + arg1);

                    }
                });

                d.show();

                return false;
            }
        });

        pref_font_size.setSummary(String.valueOf(font_size));



        pref_icon_size = findPreference("pref_icon_size");
        pref_icon_size.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {


                LayoutInflater inflater = LayoutInflater.from(context);
                View seekbarView = inflater.inflate(R.layout.seekbar_chooser_dialog, null);

                final AlertDialog.Builder db = new Builder(context);

                final SeekBar sb = (SeekBar) seekbarView.findViewById(R.id.seekBarSize);

                final TextView tv = (TextView) seekbarView.findViewById(R.id.seekbarTextView);

                db.setTitle(R.string.pref_icon_size_title).setCancelable(true).setView(seekbarView).setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        icon_size = sb.getProgress();

                        editor.putInt("pref_icon_size", icon_size);

                        editor.commit();

                        if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                            result = RESULT_OK;
                        }

                        pref_icon_size.setSummary(String.valueOf(icon_size));

                        return;
                    }
                }).setNegativeButton(R.string.button_cancel, null);


                AlertDialog d = db.create();



                Button bl = (Button) seekbarView.findViewById(R.id.button_less);

                Button bm = (Button) seekbarView.findViewById(R.id.button_more);

                bl.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        sb.setProgress(sb.getProgress() - 1);
                        tv.setText(getString(R.string.pref_icon_size) + ": " + sb.getProgress());
                    }
                });

                bm.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        sb.setProgress(sb.getProgress() + 1);
                        tv.setText(getString(R.string.pref_icon_size) + ": " + sb.getProgress());
                    }
                });

                tv.setText(getString(R.string.pref_icon_size) + ": " + icon_size);


                sb.setProgress(icon_size);
                sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar arg0) {
                    }

                    @Override
                    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {

                        tv.setText(getString(R.string.pref_icon_size) + ": " + arg1);
                    }
                });

                d.show();

                return false;
            }
        });


        pref_icon_size.setSummary(String.valueOf(icon_size));




        pref_home_position = (ListPreference) findPreference("pref_home_position");

        pref_home_position.setValueIndex(home_position);

        pref_home_position.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {

                home_position = Integer.parseInt((String) newValue);

                editor.putInt("pref_home_position", home_position);


                editor.commit();

                if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                    result = RESULT_OK;
                }

                pref_home_position.setSummary(pref_home_position.getEntries()[home_position]);


                pref_home_position.setValueIndex(home_position);

                return false;
            }
        });


        pref_home_position.setSummary(pref_home_position.getEntries()[home_position]);

        if (!show_home_button) {
            pref_home_position.setEnabled(false);
        } else {
            pref_home_position.setEnabled(true);
        }

        pref_show_home = findPreference("pref_show_home");
        ((CheckBoxPreference) pref_show_home).setChecked(show_home_button);
        pref_show_home.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                CheckBoxPreference cbp = (CheckBoxPreference) preference;
                cbp.setChecked(!cbp.isChecked());
                return false;
            }
        });

        pref_show_home.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                boolean b = Boolean.parseBoolean(newValue.toString());

                show_home_button = b;

                editor.putBoolean("pref_show_home", show_home_button);

                if (!show_home_button) {
                    pref_home_position.setEnabled(false);
                } else {
                    pref_home_position.setEnabled(true);
                }

                editor.commit();

                if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                    result = RESULT_OK;
                }

                return false;
            }
        });



        pref_remember_position = findPreference("pref_remember_position");
        ((CheckBoxPreference) pref_remember_position).setChecked(remember_position);
        pref_remember_position.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                CheckBoxPreference cbp = (CheckBoxPreference) preference;
                cbp.setChecked(!cbp.isChecked());
                return false;
            }
        });

        pref_remember_position.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                boolean b = Boolean.parseBoolean(newValue.toString());

                remember_position = b;

                editor.putBoolean("pref_remember_position", remember_position);

                editor.commit();

                if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                    result = RESULT_OK;
                }

                return false;
            }
        });



        pref_fullscreen = findPreference("pref_fullscreen");
        ((CheckBoxPreference) pref_fullscreen).setChecked(fullscreen);
        pref_fullscreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                CheckBoxPreference cbp = (CheckBoxPreference) preference;
                cbp.setChecked(!cbp.isChecked());
                return false;
            }
        });

        pref_fullscreen.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                boolean b = Boolean.parseBoolean(newValue.toString());

                fullscreen = b;

                editor.putBoolean("pref_fullscreen", fullscreen);

                editor.commit();

                result = RESULT_NEED_RESTART;

                return false;
            }
        });




        pref_restore_default = findPreference("pref_restore_default");
        pref_restore_default.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {

                AlertDialog.Builder db = new Builder(context);
                db.setCancelable(true).setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.clear();
                        editor.commit();
                        result = RESULT_NEED_RESTART_AND_CLEAR_DB;
                        getBackAndCommit();
                    }
                }).setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }).setMessage(R.string.restore_default_confirmation).setTitle(R.string.restore_default_title).show();

                return false;
            }
        });



        pref_hidden_apps = findPreference("pref_hidden_apps");
        pref_hidden_apps.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {

                startActivityForResult(new Intent(context, JakedUp.AppDrawer.HiddenApps.class), RESULT_UNHIDE_APP);

                return false;
            }
        });


//        pref_service = findPreference("pref_service");
//        ((CheckBoxPreference) pref_service).setChecked(service);
//        pref_service.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                CheckBoxPreference cbp = (CheckBoxPreference) preference;
//                cbp.setChecked(!cbp.isChecked());
//                return false;
//            }
//        });

//        pref_service.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//
//                boolean b = Boolean.parseBoolean(newValue.toString());
//
//                service = b;
//
//                editor.putBoolean("pref_service", service);
//
//                editor.commit();
//
//                if (result != NEED_RESTART && result != UNHIDE_APP) {
//                    result = RESULT_OK;
//                }
//
//                return false;
//            }
//        });


        pref_backups = (PreferenceScreen) findPreference("pref_backups");

        pref_export = findPreference("pref_export");
        pref_export.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {


                Calendar cal = Calendar.getInstance();
                Integer year = cal.get(Calendar.YEAR);
                Integer month = (cal.get(Calendar.MONTH) + 1);
                Integer day = cal.get(Calendar.DAY_OF_MONTH);
                Integer hour = cal.get(Calendar.HOUR_OF_DAY);
                Integer minutes = cal.get(Calendar.MINUTE);

                String backupDate = year + "_" + ((month.toString().length() == 1) ? "0" : "") + month
                        + "_" + ((day.toString().length() == 1) ? "0" : "") + day
                        + " " + ((hour.toString().length() == 1) ? "0" : "") + hour
                        + "." + ((minutes.toString().length() == 1) ? "0" : "") + minutes;


                File backup_folder = new File(Environment.getExternalStorageDirectory() + BACKUPS_DIR + backupDate);
                int copyIndex = 2;
                while (backup_folder.exists()) {
                    backup_folder = new File(Environment.getExternalStorageDirectory() + BACKUPS_DIR + backupDate + " #" + copyIndex);
                    copyIndex++;
                }
                try {
                    File prefs_file = new File("/data/data/JakedUp.AppDrawer/shared_prefs/", "AppDrawer_Prefs.xml");
                    File prefs_copy = new File(backup_folder, "AppDrawer_Prefs.xml");
                    backup_folder.mkdirs();
                    FileInputStream from = new FileInputStream(prefs_file);
                    FileOutputStream to = new FileOutputStream(prefs_copy);


                    IOUtils.copy(from, to);

//                    Toast.makeText(getApplicationContext(), R.string.exported,
//                            Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), R.string.error_exporting,
                            Toast.LENGTH_LONG).show();
                    Log.e("AD_EXPORT", "Error exporting preferences", e);
                }

                try {
                    File db_file = new File("/data/data/JakedUp.AppDrawer/databases/", "AppDrawer_DB");
                    File db_copy = new File(backup_folder, "AppDrawer_DB.db");
                    backup_folder.mkdirs();
                    FileInputStream dbFrom = new FileInputStream(db_file);
                    FileOutputStream dbTo = new FileOutputStream(db_copy);


                    IOUtils.copy(dbFrom, dbTo);

//                    Toast.makeText(getApplicationContext(), R.string.exported_DB,
//                            Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), R.string.error_exporting_DB,
                            Toast.LENGTH_LONG).show();
                    Log.e("AD_EXPORT", "Error exporting database", e);
                }


//                reloadBackups();

                pref_backups.onItemClick(null, null, pref_import.getOrder(), 0);

                return false;


            }
        });


        pref_import = (PreferenceScreen) findPreference("pref_import");
        pref_import.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                reloadBackups();
                return false;
            }
        });
        reloadBackups();

    }

    private void getBackAndCommit() {
        if (intentResult == null) {
            intentResult = getIntent();
        }
        setResult(result, intentResult);
        finish();

    }

    //Commit changes
    public void onBackPressed() {

        getBackAndCommit();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_IMAGE_SELECT:
                if (resultCode == RESULT_OK) {
                    try {


                        //	            Uri selectedImage = data.getData();
                        //	            String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        //
                        //	            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                        //	            cursor.moveToFirst();
                        //
                        //	            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        //	            String filePath = cursor.getString(columnIndex);
                        //	            cursor.close();


                        String filePath = Environment.getExternalStorageDirectory()
                                + BG_IMG_FILE;


                        bg_image = filePath;

                        editor.putString("pref_bg_image", bg_image);

                        editor.commit();

                        if (result != RESULT_NEED_RESTART && result != RESULT_UNHIDE_APP) {
                            result = RESULT_OK;
                        }



                    } catch (NullPointerException npe) {
                        Toast.makeText(getApplicationContext(), String.valueOf("null"),
                                Toast.LENGTH_LONG).show();
                    }

                }
            case RESULT_UNHIDE_APP:
                if (resultCode == RESULT_UNHIDE_APP) {
                    if (result != RESULT_NEED_RESTART) {
                        result = RESULT_UNHIDE_APP;
                        intentResult = data;
                    }
                }
                break;
        }

    }

    public static String[] checkNewVersion() {
        try {

            String installedVersion = Main.pm.getPackageInfo("JakedUp.AppDrawer", 0).versionName;

            URL updateURL = null;
            if (installedVersion.contains("b")) {
                updateURL = new URL("http://dl.dropbox.com/u/1066873/AltDrawer/last_dev_version.txt");
            } else {
                updateURL = new URL("http://dl.dropbox.com/u/1066873/AltDrawer/last_version.txt");
            }


            URLConnection conn = updateURL.openConnection();

            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String latestVersion = reader.readLine();
            String latestVersionURL = reader.readLine();

            in.close();



            if (isNewer(installedVersion, latestVersion)) {
                return new String[]{latestVersion, latestVersionURL};
            } else {
                return null;
            }


        } catch (Exception e) {

            System.err.println("NEW VERSION ERROR: " + e);

        }
        return null;
    }

    private static boolean isNewer(String installedVersion, String latestVersion) {

        String[] iv = installedVersion.split("\\.");
        String[] lv = latestVersion.split("\\.");

        if (lv[0].compareTo(iv[0]) > 0) {
            return true;
        } else if (lv[0].compareTo(iv[0]) == 0) {
            if (lv[1].compareTo(iv[1]) > 0) {
                return true;
            } else if (lv[1].compareTo(iv[1]) == 0) {
                if (lv.length == 3) {
                    if (iv.length == 2) {
                        return true;
                    } else if (iv.length == 3) {
                        if (lv[2].compareTo(iv[2]) > 0) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;

    }

    private void reloadBackups() {
        pref_import.removeAll();
        File backups_folder = new File(Environment.getExternalStorageDirectory() + BACKUPS_DIR);
        File[] dirs = backups_folder.listFiles();
        if (dirs != null) {
            for (final File f : dirs) {
                if (f.isDirectory()) {
                    final Preference bak = new Preference(context);
                    bak.setTitle(f.getName().replace("_", "/").replace(".", ":"));

                    bak.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                        public boolean onPreferenceClick(Preference preference) {

                            AlertDialog.Builder adb = new Builder(context);

                            adb.setItems(new CharSequence[]{
                                        getResources().getString(R.string.restore_backup),
                                        getResources().getString(R.string.rename_backup),
                                        getResources().getString(R.string.delete_backup)}, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            AlertDialog.Builder adb2 = new Builder(context);
                                            adb2.setCancelable(true).setTitle(R.string.pref_import).setNegativeButton(R.string.button_cancel, null).setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    File prefs_copy = new File("/data/data/JakedUp.AppDrawer/shared_prefs/", "AppDrawer_Prefs.xml");
                                                    File prefs_file = new File(f, "AppDrawer_Prefs.xml");
                                                    File db_copy = new File("/data/data/JakedUp.AppDrawer/databases/", "AppDrawer_DB");
                                                    File db_file = new File(f, "AppDrawer_DB.db");
                                                    try {
                                                        FileInputStream from = new FileInputStream(prefs_file);
                                                        FileOutputStream to = new FileOutputStream(prefs_copy);
                                                        prefs_copy.mkdirs();

                                                        IOUtils.copy(from, to);
                                                        result = RESULT_NEED_RESTART;

//                                                        Toast.makeText(getApplicationContext(), R.string.imported,
//                                                                Toast.LENGTH_LONG).show();

                                                    } catch (Exception e) {

                                                        Toast.makeText(getApplicationContext(), R.string.prefs_not_found,
                                                                Toast.LENGTH_LONG).show();

                                                        e.printStackTrace(System.err);
                                                    }

                                                    try {
                                                        FileInputStream dbFrom = new FileInputStream(db_file);
                                                        FileOutputStream dbTo = new FileOutputStream(db_copy);
                                                        db_copy.mkdirs();

                                                        IOUtils.copy(dbFrom, dbTo);
                                                        result = RESULT_NEED_RESTART;

//                                                        Toast.makeText(getApplicationContext(), R.string.imported_DB,
//                                                                Toast.LENGTH_LONG).show();

                                                    } catch (IOException e) {

                                                        Toast.makeText(getApplicationContext(), R.string.db_not_found,
                                                                Toast.LENGTH_LONG).show();

                                                        e.printStackTrace(System.err);
                                                    }

                                                    if (result == RESULT_NEED_RESTART) {
                                                        getBackAndCommit();
                                                    }

                                                }
                                            }).setMessage(R.string.import_confirmation).create().show();
                                            break;
                                        case 1:
                                            AlertDialog.Builder adb3 = new Builder(context);
                                            final EditText name = new EditText(context);
                                            name.setText(f.getName());
                                            adb3.setTitle(R.string.rename_backup).setCancelable(true).setView(name).setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    if (f.renameTo(new File(Environment.getExternalStorageDirectory() + BACKUPS_DIR + name.getText().toString()))) {
                                                        reloadBackups();
                                                    }
                                                }
                                            }).setNegativeButton(R.string.button_cancel, null).create().show();

                                            break;
                                        case 2:
                                            if (deleteDirectory(f)) {
                                                pref_import.removePreference(bak);
                                                if (pref_import.getPreferenceCount() == 0) {
                                                    Dialog d = pref_import.getDialog();
                                                    if (d != null) {
                                                        d.dismiss();
                                                    }
                                                }

                                            }

                                    }

                                    dialog.dismiss();
                                }
                            }).create().show();



                            return false;
                        }
                    });
                    pref_import.addPreference(bak);
                }
            }
        }
    }

    private boolean deleteDirectory(File f) {

        try {
            for (File inside : f.listFiles()) {
                if (inside.isDirectory()) {
                    deleteDirectory(inside);
                } else {
                    inside.delete();
                }
            }

            return f.delete();
        } catch (Exception e) {
            return false;
        }



    }
}
