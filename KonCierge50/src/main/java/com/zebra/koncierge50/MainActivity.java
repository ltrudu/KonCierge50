package com.zebra.koncierge50;
import java.io.File;

import android.animation.ValueAnimator;
import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.animation.Animation;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@RequiresApi(api = Build.VERSION_CODES.R)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "KonCierge50";

    private LinearLayout llPeopleCard;

    private ImageView imgStar;
    private Button printButton;
    private Button cancelButton;
    private AutoCompleteTextView actTextView;
    private ImageView imageViewLogo;
    private ImageView imageViewZebra;
    private ValueAnimator pulseAnimator;

    private LinearLayout llNewCard;
    private Button btCreateManually;
    private Button btNewCardPrint;
    private Button btNewCardClose;
    private EditText txtNewPrenom;
    private EditText txtNewNom;
    private EditText txtNewSociete;
    private EditText txtNewMobile;
    private EditText txtNewEmail;
    private EditText txtNewFonction;

    private CardPrintingHelper mCardPrintingHelper;

    private WristbandPrintingHelper mWristbandPrintingHelper;

    private Boolean isVIP;
    
    private CSVDataContainer mCSVDataContainer;
    private List<String> mFilteredList;
    private CSVDataModel mSelectedModel;


    private LEDBarLightServiceHelper mLEDBarLightServiceHelper;
    private ValueAnimator mColorAnimator;
    

    private SetupConfigurationClass mSetupConfigurationClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        // Enable immersive mode
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        // Configure the behavior of the hidden system bars.
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        // Prevent navigation bar to appear when the soft keyboard is on screen
        View decorView = getWindow().getDecorView();
        decorView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsets onApplyWindowInsets(@NonNull View view, @NonNull WindowInsets windowInsets) {
                if (windowInsets.isVisible(WindowInsets.Type.ime())) {
                    windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());
                }
                return windowInsets;
            }
        });
        // Handle keyboard visibility changes
        ViewCompat.setOnApplyWindowInsetsListener(decorView, (v, insets) -> {
            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());
            }
            return insets;
        });

        //TODO: use MX feature to disable navigation bar
        // https://techdocs.zebra.com/mx/uimgr/#navigation-bar-enabledisable

        imageViewLogo = findViewById(R.id.imageViewLogo);
        imageViewZebra = findViewById(R.id.imageViewZebra);


        llNewCard = findViewById(R.id.llNewCard);
        txtNewNom = findViewById(R.id.txtNewNom);
        autoCapitalizeContent(txtNewNom);
        txtNewPrenom = findViewById(R.id.txtNewPrenom);
        autoCapitalizeContent(txtNewPrenom);
        txtNewSociete = findViewById(R.id.txtNewSociete);
        autoCapitalizeContent(txtNewSociete);
        txtNewMobile = findViewById(R.id.txtNewMobile);
        txtNewEmail = findViewById(R.id.txtNewEmail);
        txtNewFonction = findViewById(R.id.txtNewFonction);
        autoCapitalizeContent(txtNewFonction);
        btCreateManually = findViewById(R.id.btNewCardCreate);



        llPeopleCard = findViewById(R.id.llPeopleCard);
        imgStar = findViewById(R.id.imgStar);

        printButton = findViewById(R.id.btPrint);
        printButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mSetupConfigurationClass.EXPORT_ATTENDEE_DATA && mSelectedModel != null)
                {
                    Date nowDate = new Date();
                    try {
                        ExportDataUtils.appendDataToCSVFile(mSelectedModel, nowDate, false);
                    }
                    catch(Exception e)
                    {
                        Toast.makeText(getApplicationContext(), MainActivity.this.getString(R.string.error) + ": " +  e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }
                if(mSetupConfigurationClass.DISABLE_CARD_PRINTING)
                {
                    hideInfoCard();
                }
                else {
                    print_card();
                }
            }
        });

        cancelButton = findViewById(R.id.btCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideInfoCard();
            }
        });

        btCreateManually = findViewById(R.id.btNewCardCreate);
        btCreateManually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewCard();
            }
        });

        btNewCardClose = findViewById(R.id.btNewCardFermer);
        btNewCardClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideNewCard();
            }
        });

        btNewCardPrint = findViewById(R.id.btNewCardImprimer);
        btNewCardPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtNewPrenom.getText().toString().isEmpty())
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.error)
                            .setMessage(R.string.enter_firstname)
                            .setPositiveButton(R.string.yes, null)
                            .setNeutralButton(R.string.maybe, null)
                            .setNegativeButton(R.string.no, null)
                            .show();
                    return;
                }
                else if(txtNewNom.getText().toString().isEmpty())
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.error)
                            .setMessage(R.string.enter_lastname)
                            .setPositiveButton(R.string.certainly, null)
                            .setNeutralButton(R.string.with_pleasure, null)
                            .setNegativeButton(R.string.without_me, null)
                            .show();
                    return;
                }
                else if(txtNewSociete.getText().toString().isEmpty())
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.error)
                            .setMessage(R.string.enter_company)
                            .setPositiveButton(R.string.yes, null)
                            .show();
                    return;
                }
                mSelectedModel = new CSVDataModel();
                mSelectedModel.Prenom = txtNewPrenom.getText().toString();
                mSelectedModel.Nom = txtNewNom.getText().toString();
                mSelectedModel.Societe = txtNewSociete.getText().toString();
                mSelectedModel._AllText =  (mSelectedModel.Prenom + " " + mSelectedModel.Nom).toLowerCase();
                mSelectedModel.Email = txtNewEmail.getText().toString();
                mSelectedModel.Mobile = txtNewMobile.getText().toString();
                mSelectedModel.Fonction = txtNewFonction.getText().toString();
                mSelectedModel.VIP = false;
                mSelectedModel.createVCard();

                if(mSetupConfigurationClass.EXPORT_REGISTERED_DATA) {
                    Date nowDate = new Date();
                    try {
                        ExportDataUtils.appendDataToCSVFile(mSelectedModel, nowDate, true);
                    }
                    catch(Exception e)
                    {
                        Toast.makeText(getApplicationContext(), MainActivity.this.getString(R.string.error) + ": " +  e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }
                if(mSetupConfigurationClass.DISABLE_CARD_PRINTING)
                {
                    hideNewCard();
                }
                else {
                    print_new_card();
                }
            }
        });

        actTextView = findViewById(R.id.actTextView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line);
        actTextView.setAdapter(adapter);
        actTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchText = charSequence.toString(); //editable.toString().toLowerCase();
                // We start the search at two characters
                // otherwise the list would be too long
                if(searchText.length() >= mSetupConfigurationClass.NUMBER_OF_CHARACTERS_BEFORE_SEARCHING_FOR_CANDIDATES) {
                    mFilteredList = mCSVDataContainer.getCandidates(searchText);
                    ArrayAdapter<String> filteredAdapter = new ArrayAdapter<>(MainActivity.this,
                            android.R.layout.simple_dropdown_item_1line, mFilteredList);
                    actTextView.setAdapter(filteredAdapter);
                    filteredAdapter.notifyDataSetChanged();
                    actTextView.showDropDown();
                }
                else
                {
                    // Empty the filtered list
                    mFilteredList = new ArrayList<>();
                    ArrayAdapter<String> filteredAdapter = new ArrayAdapter<>(MainActivity.this,
                            android.R.layout.simple_dropdown_item_1line, mFilteredList);
                    actTextView.setAdapter(filteredAdapter);
                    filteredAdapter.notifyDataSetChanged();
                }
                // We changed the text this invalidate the selection and disable print button
                mSelectedModel = null;
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        actTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
                // As soon as we click on a specific entry it sets the data of the
                // actTextView, and trigger an afterTextChanged event
                // this will filter the mFilteredList to one item i.e. what has been selected
                // so we don't care about position here, we need only the first item in the list
                // since the list has only one item now
                String selectedItem = (String) parent.getItemAtPosition(0);
                mSelectedModel = mCSVDataContainer.findItemWithAllText(selectedItem);

                // Remove focus from AutoCompleteTextView
                actTextView.clearFocus();

                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(actTextView.getWindowToken(), 0);

                // Update card entry
                // Hide text search and display info card
                showInfoCard();
            }
        });
//        actTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean hasFocus) {
//                if(hasFocus)
//                {
//                    showKeyboard();
//                }
//                else
//                {
//                    hideKeyboard();
//                }
//            }
//        });
    }



    private void autoCapitalizeContent(EditText editText)
    {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus == false)
                {
                    EditText current = (EditText)view;
                    String content = current.getText().toString();
                    if(content.isEmpty())
                    {
                        return;
                    }
                    current.setText(StringHelpers.capitalizeFirstLetters(content));
                }
            }
        });

    }

    private void showInfoCard() {
        // Update card entry
        // Hide text search and display info card
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopLEDColorAnimation();
                updateInfoCard();
                hideLogos();
                if(mSetupConfigurationClass.ENABLE_SEARCH_MODE)
                    actTextView.setVisibility(View.INVISIBLE);
                btCreateManually.setVisibility(View.INVISIBLE);
                hideKeyboard();
                printButton.setVisibility(View.VISIBLE);
                animateView(llPeopleCard, 0, 0, -llPeopleCard.getWidth() - 200, 0, false);
            }
        });

    }

    private void hideInfoCard()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopStarAnimation();
                showLogos();
                animateView(llPeopleCard, 0, 0, 0, llPeopleCard.getWidth() + 200, true);
                if(mSetupConfigurationClass.ENABLE_SEARCH_MODE) {
                    actTextView.setText("");
                    actTextView.setVisibility(View.VISIBLE);
                    actTextView.requestFocus();
                }
                btCreateManually.setVisibility(mSetupConfigurationClass.CAN_CREATE_CARD ? View.VISIBLE : View.GONE);
                startLEDColorAnimation();
            }
        });

    }


    private void showNewCard() {
        // Update card entry
        // Hide text search and display info card
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopLEDColorAnimation();
                hideLogos();
                hideKeyboard();
                btNewCardPrint.setVisibility(View.VISIBLE);
                txtNewNom.setText("");
                txtNewPrenom.setText("");
                txtNewSociete.setText("");
                if(mSetupConfigurationClass.CAN_CREATE_VCARD) {
                    txtNewEmail.setText("");
                    txtNewMobile.setText("");
                    txtNewFonction.setText("");
                    txtNewEmail.setVisibility(View.VISIBLE);
                    txtNewMobile.setVisibility(View.VISIBLE);
                    txtNewFonction.setVisibility(View.VISIBLE);
                }
                else
                {
                    txtNewEmail.setVisibility(View.GONE);
                    txtNewMobile.setVisibility(View.GONE);
                    txtNewFonction.setVisibility(View.GONE);
                }
                if(mSetupConfigurationClass.ENABLE_SEARCH_MODE)
                    actTextView.setVisibility(View.INVISIBLE);
                btCreateManually.setVisibility(View.INVISIBLE);
                animateView(llNewCard, 0, 0, -llNewCard.getWidth() - 200, 0, false);
            }
        });

    }

    private void hideNewCard()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLogos();
                animateView(llNewCard, 0, 0, 0, llNewCard.getWidth() + 200, true);
                if(mSetupConfigurationClass.ENABLE_SEARCH_MODE) {
                    actTextView.setText("");
                    actTextView.setVisibility(View.VISIBLE);
                    actTextView.requestFocus();
                }
                btCreateManually.setVisibility(mSetupConfigurationClass.CAN_CREATE_CARD ? View.VISIBLE : View.INVISIBLE);
                startLEDColorAnimation();
            }
        });

    }

    private void showKeyboard()
    {
        View view = this.getCurrentFocus();
        if(view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        imageViewLogo.setVisibility(View.GONE);
        load_logo();
        imageViewLogo.setVisibility(View.VISIBLE);
        read_config();
        if(mSetupConfigurationClass.ENABLE_SEARCH_MODE == false)
            actTextView.setVisibility(TextView.GONE);

        if(mSetupConfigurationClass.DISABLE_CARD_PRINTING)
        {
            printButton.setText(R.string.register);
            btNewCardPrint.setText(R.string.register);
        }

        // Create helper classes
        mCardPrintingHelper = new CardPrintingHelper(this, mSetupConfigurationClass);
        // Force copying assets font to Koncierge50 for later upgrade
        mCardPrintingHelper.getCustomFont();
        mWristbandPrintingHelper = new WristbandPrintingHelper(this,mSetupConfigurationClass.WRISTBAND_PRINTER_IP,mSetupConfigurationClass.WRISTBAND_PRINTER_PORT);
        mCSVDataContainer = new CSVDataContainer();

        if(Build.MODEL.equalsIgnoreCase(Constants.KC50_BUILD_MODEL)) {
            initializeLEDBarServiceForKC50();
       }

        read_csv();
        llPeopleCard.setVisibility(View.GONE);
        animateView(llPeopleCard, 0, 0, 0, llPeopleCard.getWidth() + 200, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Cleanup helper classes members to trigger Garbage collector
        mCardPrintingHelper = null;
        mWristbandPrintingHelper = null;
        mCSVDataContainer.clean();
        mCSVDataContainer = null;
        if(mLEDBarLightServiceHelper != null)
        {
            stopLEDColorAnimation();
            mLEDBarLightServiceHelper.cleanup();
            mLEDBarLightServiceHelper = null;
        }
    }

    private void load_logo()
    {
        File demoDataFolder = new File(Constants.DEMO_DATA_FOLDER);
        if(demoDataFolder.exists() == false)
        {
            demoDataFolder.mkdirs();
        }
        File logoPath = new File(demoDataFolder, Constants.LOGO_FILENAME);
        if(logoPath.exists() == false)
        {
            FileUtils.copyDrawableToFolder(this, R.drawable.kc50, Constants.LOGO_FILENAME, Constants.DEMO_DATA_FOLDER);
        }
        Bitmap bitmap = BitmapFactory.decodeFile(logoPath.getAbsolutePath());
        // Trouver la View par son ID et définir le Bitmap comme background
        imageViewLogo.setBackground(new BitmapDrawable(this.getResources(), bitmap));
    }

    private void read_config()
    {
        File demoDataFolderFile = new File(Constants.DEMO_DATA_FOLDER);
        if(demoDataFolderFile.exists() == false)
        {
            // The data folder does not exists
            // This may be the first time the app is launched
            // or it is launched in demo mode
            // lets create the folder structure
            demoDataFolderFile.mkdirs();
        }

        File configFile = new File(demoDataFolderFile, Constants.CONFIG_FILENAME);
        if(configFile.exists() == false)
        {
            // We do not have data in the persist folder
            // Let's copy a demo file from asset folder
            try {
                mSetupConfigurationClass = new SetupConfigurationClass();
                mSetupConfigurationClass.saveToFile(configFile.getPath());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
        mSetupConfigurationClass = SetupConfigurationClass.loadFromFile(configFile.getPath());

        btCreateManually.setVisibility(mSetupConfigurationClass.CAN_CREATE_CARD ? View.VISIBLE : View.GONE);
    }

    private void initializeLEDBarServiceForKC50()
    {
        mLEDBarLightServiceHelper = new LEDBarLightServiceHelper(this);
        initializeColorAnimator();
        mLEDBarLightServiceHelper.initialize(new LEDBarLightServiceHelper.LEDBarLightServiceInitializeCallback() {
            @Override
            public void onInitialized() {
                startLEDColorAnimation();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, message);
                Toast.makeText(MainActivity.this, "Error initializing LEDBAR.\n" + message, Toast.LENGTH_LONG).show();
            }
        });
        startLEDColorAnimation();
    }

    private void initializeColorAnimator() {
        // Create a ValueAnimator that animates between the start and end colors
        mColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.RED, Color.GREEN, Color.BLUE);
        mColorAnimator.setDuration(5000); // Duration of the animation in milliseconds
        mColorAnimator.setRepeatCount(ValueAnimator.INFINITE); // Repeat the animation indefinitely
        mColorAnimator.setRepeatMode(ValueAnimator.REVERSE); // Reverse the animation at the end

        // Update the color of the TextView on each animation frame
        mColorAnimator.addUpdateListener(animator -> {
            int animatedValue = (int) animator.getAnimatedValue();
            mLEDBarLightServiceHelper.setColorARGB(animatedValue);
        });
    }

    private void setAndroidColor(int androidColor)
    {
        if(mLEDBarLightServiceHelper != null)
        {
            mLEDBarLightServiceHelper.setAndroidColor(androidColor);
        }
    }

    private void startLEDColorAnimation()
    {
        if(mColorAnimator != null)
        {
            mColorAnimator.start();
        }
    }

    private void stopLEDColorAnimation()
    {
        if(mColorAnimator != null)
        {
            mColorAnimator.cancel();
        }
    }

    private void print_new_card()
    {
        if(mSelectedModel != null) {

            btNewCardPrint.setVisibility(View.GONE);
            mCardPrintingHelper.print(mSelectedModel, new CardPrintingHelper.CardPrintingHelperCallback() {
                @Override
                public void onMessage(String message) {
                    Log.v(TAG, message);
                }

                @Override
                public void onSuccess() {
                    Log.v(TAG, "New Card printed with success.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideNewCard();
                        }
                    });

                }

                @Override
                public void onError(String message) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), MainActivity.this.getString(R.string.error) + ": " +  message, Toast.LENGTH_LONG).show();
                            Log.e(TAG, message);
                        }
                    });
                }
            });
        }
    }


    private void print_card()
    {
        if(mSelectedModel != null) {
            printButton.setVisibility(View.GONE);
            mCardPrintingHelper.print(mSelectedModel, new CardPrintingHelper.CardPrintingHelperCallback() {
                @Override
                public void onMessage(String message) {
                    Log.v(TAG, message);
                }

                @Override
                public void onSuccess() {
                    Log.v(TAG, "Card printed with success.");
                    if(mSetupConfigurationClass.VIP_MODE && mSetupConfigurationClass.PRINT_WRISTBAND_FOR_VIP && isVIP == true) {
                        print_wristband();
                    }
                    else {
                        hideInfoCard();
                    }
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), MainActivity.this.getString(R.string.error) + ": " +  message, Toast.LENGTH_LONG).show();
                            Log.e(TAG, message);
                        }
                    });
                }
            });
        }
    }

    private void print_wristband(){
        mWristbandPrintingHelper.print(new WristbandPrintingHelper.WristbandPrintingHelperCallback(){
            @Override
            public void onMessage(String message) {
                Log.v(TAG, message);
            }
            @Override
            public void onSuccess() {
                Log.v(TAG, "Wristband printed with success.");
                hideInfoCard();
            }
            public void onError(String message) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), MainActivity.this.getString(R.string.error) + ": " +  message, Toast.LENGTH_LONG).show();
                        Log.e(TAG, message);
                    }
                });
            }
        });

    }

    private void read_csv()
    {
        File demoDataFolderFile = new File(Constants.DEMO_DATA_FOLDER);
        if(demoDataFolderFile.exists() == false)
        {
            // The data folder does not exists
            // This may be the first time the app is launched
            // or it is launched in demo mode
            // lets create the folder structure
            demoDataFolderFile.mkdirs();
        }

        File csvFile = new File(demoDataFolderFile, Constants.CSV_FILENAME);
        if(csvFile.exists() == false)
        {
            // We do not have data in the persist folder
            // Let's copy a demo file from asset folder
            try {
                FileUtils.copyAssetToFolder(this, "Attendees.csv", Constants.CSV_FILENAME, Constants.DEMO_DATA_FOLDER);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
        mCSVDataContainer.readFile(csvFile);
    }

    private void updateInfoCard()
    {
        // We don't store these controls in members since there is no
        // performance issue here
        // We just update them on the fly ;)
        // Laziness
        if(mSelectedModel != null) {
            ((TextView) findViewById(R.id.tvNomPrenom)).setText(mSelectedModel.Prenom + " " + mSelectedModel.Nom);
            ((TextView) findViewById(R.id.tvSociete)).setText(mSelectedModel.Societe);
            if(mSetupConfigurationClass.VIP_MODE == true && mSelectedModel.VIP == true)
            {
                imgStar.setVisibility(View.VISIBLE);
                startStarAnimation();
                stopLEDColorAnimation();
                setAndroidColor(Color.GREEN);
                isVIP = true;
            }
            else
            {
                imgStar.setVisibility(View.GONE);
                stopStarAnimation();
                stopLEDColorAnimation();
                setAndroidColor(Color.BLUE);
                isVIP = false;
            }
        }
    }


    public void hideLogos() {
        animateView(imageViewLogo, 0, -imageViewLogo.getHeight() - 100, 0, 0, true);
        animateView(imageViewZebra, 0, imageViewZebra.getHeight() + 100, 0, 0, true);
    }

    public void showLogos() {
        animateView(imageViewLogo, -imageViewLogo.getHeight() + 100, 0, 0, 0, false);
        animateView(imageViewZebra, imageViewZebra.getHeight() + 100, 0, 0, 0, false);
    }

    private void animateView(final View view, float fromY, float toY, float fromX, float toX, final boolean hide) {
                TranslateAnimation animation = new TranslateAnimation(
                        Animation.ABSOLUTE, fromX,
                        Animation.ABSOLUTE, toX,
                        Animation.ABSOLUTE, fromY,
                        Animation.ABSOLUTE, toY);
                animation.setDuration(500); // You can adjust the duration as needed
                animation.setFillAfter(true);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (hide) {
                            view.setVisibility(View.GONE);
                            view.setActivated(false);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                if (!hide) {
                    view.setVisibility(View.VISIBLE);
                    view.setActivated(true);
                }
                view.startAnimation(animation);
            }

    public void startStarAnimation() {
        if (pulseAnimator != null && pulseAnimator.isRunning()) {
            return;
        }

        float maxScale = 1.2f;
        float minScale = 0.8f;
        long duration = 1000;

        pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        pulseAnimator.setDuration(duration);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        pulseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                float scale = minScale + (maxScale - minScale) * animatedValue;
                imgStar.setScaleX(scale);
                imgStar.setScaleY(scale);
            }
        });

        pulseAnimator.start();
    }

    public void stopStarAnimation() {
    if (pulseAnimator != null && pulseAnimator.isRunning()) {
                pulseAnimator.cancel();
                imgStar.setScaleX(1f);
                imgStar.setScaleY(1f);
            }

    }
}