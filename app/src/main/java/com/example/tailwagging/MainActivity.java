package com.example.tailwagging;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.IValue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private Spinner petTypeDropdown;
    private TextInputEditText symptomsInput;
    private MaterialButton classifyButton;
    private TextView resultTextView;
    private Module model;
    private Map<String, Integer> petTypeMap; // Maps pet types to encoded values
    private Map<String, Integer> symptomMap; // Maps symptoms to encoded values

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        petTypeDropdown = findViewById(R.id.petTypeDropdown);
        symptomsInput = findViewById(R.id.symptomsInput);
        classifyButton = findViewById(R.id.classifyButton);
        resultTextView = findViewById(R.id.resultTextView);

        // Load the PyTorch model
        loadModel();

        // Initialize pet type and symptom mappings
        initializeMappings();

        // Set up the pet type dropdown
        setupPetTypeDropdown();

        // Set click listener for the classify button
        classifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                classifyDisease();
            }
        });
    }

    private void loadModel() {
        try {
            // Load the model from the assets folder
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("animal_disease_classifier.pt");
            File modelFile = new File(getCacheDir(), "animal_disease_classifier.pt");
            copyFile(inputStream, modelFile);
            model = Module.load(modelFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ModelError", "Failed to load model: " + e.getMessage());
        }
    }

    private void copyFile(InputStream inputStream, File outputFile) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }

    private void initializeMappings() {
        // Map pet types to encoded values
        petTypeMap = new HashMap<>();
        petTypeMap.put("Dog", 0);
        petTypeMap.put("Cat", 1);
        petTypeMap.put("Rabbit", 2);
        petTypeMap.put("Cow", 3);
        petTypeMap.put("Chicken", 4);
        petTypeMap.put("Horse", 5);
        petTypeMap.put("Turtle", 6);
        petTypeMap.put("Hamster", 7);
        petTypeMap.put("Lion", 8);
        petTypeMap.put("Fox", 9);
        petTypeMap.put("Goat", 10);
        petTypeMap.put("Deer", 11);
        petTypeMap.put("Monkey", 12);
        petTypeMap.put("Birds", 13);
        petTypeMap.put("Sheep", 14);
        petTypeMap.put("Pigs", 15);
        petTypeMap.put("Fowl", 16);
        petTypeMap.put("Duck", 17);
        petTypeMap.put("Other Birds", 18);
        petTypeMap.put("Snake", 19);
        petTypeMap.put("Donkey", 20);
        petTypeMap.put("Mules", 21);
        petTypeMap.put("Elephant", 22);
        petTypeMap.put("Tiger", 23);
        petTypeMap.put("Buffaloes", 24);

        // Map symptoms to encoded values (you can expand this based on your dataset)
        symptomMap = new HashMap<>();
        symptomMap.put("Fever", 0);
        symptomMap.put("Diarrhea", 1);
        symptomMap.put("Vomiting", 2);
        symptomMap.put("Weight loss", 3);
        symptomMap.put("Dehydration", 4);
        // Add more symptoms as needed
    }

    private void setupPetTypeDropdown() {
        // Populate the pet type dropdown
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.pet_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        petTypeDropdown.setAdapter(adapter);
    }

    private void classifyDisease() {
        // Get user inputs
        String petType = petTypeDropdown.getSelectedItem().toString();
        String symptoms = symptomsInput.getText().toString().trim(); // Trim whitespace

        // Check if symptoms input is empty
        if (symptoms.isEmpty()) {
            resultTextView.setText("Please enter symptoms");
            return;
        }

        // Preprocess inputs
        int petTypeEncoded = petTypeMap.getOrDefault(petType, 0);
        int[] symptomsEncoded = encodeSymptoms(symptoms);

        // Create input tensor
        float[] inputArray = new float[petTypeMap.size() + symptomMap.size()];
        inputArray[petTypeEncoded] = 1; // One-hot encode pet type
        for (int symptomIndex : symptomsEncoded) {
            if (symptomIndex != -1) { // Skip unknown symptoms
                inputArray[symptomIndex + petTypeMap.size()] = 1; // One-hot encode symptoms
            }
        }

        // Convert input array to a tensor
        long[] shape = {1, inputArray.length}; // Shape: [batch_size, feature_size]
        Tensor inputTensor = Tensor.fromBlob(inputArray, shape);

        // Log the input tensor shape and content for debugging
        Log.d("InputTensor", "Shape: " + inputTensor.shape()[0] + "x" + inputTensor.shape()[1]);
        Log.d("InputTensor", "Content: " + Arrays.toString(inputArray));

        // Run inference
        try {
            Tensor outputTensor = model.forward(IValue.from(inputTensor)).toTensor();
            float[] outputArray = outputTensor.getDataAsFloatArray();

            // Log the output tensor for debugging
            Log.d("OutputTensor", "Content: " + Arrays.toString(outputArray));

            // Interpret the result
            boolean isDangerous = outputArray[0] > 0.5;
            resultTextView.setText(isDangerous ? "Dangerous" : "Not Dangerous");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("InferenceError", "Failed to run inference: " + e.getMessage());
            resultTextView.setText("Error during classification");
        }
    }

    private int[] encodeSymptoms(String symptoms) {
        // Split symptoms by comma and trim whitespace
        String[] symptomList = symptoms.split(",");
        int[] encodedSymptoms = new int[symptomList.length];
        for (int i = 0; i < symptomList.length; i++) {
            String symptom = symptomList[i].trim(); // Trim whitespace
            encodedSymptoms[i] = symptomMap.getOrDefault(symptom, -1); // Use -1 for unknown symptoms
            if (encodedSymptoms[i] == -1) {
                Log.e("SymptomError", "Unknown symptom: " + symptom);
            }
        }
        return encodedSymptoms;
    }
}
