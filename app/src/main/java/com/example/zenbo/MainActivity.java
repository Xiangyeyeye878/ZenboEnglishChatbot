package com.example.zenbo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.asus.robotframework.API.ExpressionConfig;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import com.asus.robotframework.API.RobotAPI;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.SpeakConfig;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends RobotActivity {

    public static String LOG = "loogg", preferType = "everything", character = "0";
    String instruction_New_Environment;
    public static TextView tvResult;
    public static Button btnStart, btnStop, btnNew, btnEvualuate;
    public static Switch charSwitch;
    public static EditText getPrefer;
    static List<String> history = new ArrayList<>();
    static List<String> history_env = new ArrayList<>();
    static List<String> history_evu = new ArrayList<>();

    static String clearData = "";
    static String inputStr = "";


    static String apiKey = "AIzaSyB8izLGxCxBsbaNfRq2_9cNDLNdxyAB96I";
    static List<String> conversation = new ArrayList<>();
    static GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey);
    static GenerativeModelFutures model = GenerativeModelFutures.from(gm);
    static GenerativeModelFutures Environment = GenerativeModelFutures.from(gm);
    static GenerativeModelFutures evaluate = GenerativeModelFutures.from(gm);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 100);
        getPrefer = findViewById(R.id.editTextText);
        tvResult = findViewById(R.id.textView1);
        tvResult.setMovementMethod(new ScrollingMovementMethod());
        charSwitch = findViewById(R.id.switch1);
        btnEvualuate = findViewById(R.id.button3);
        btnStop = findViewById(R.id.button2);
        btnNew = findViewById(R.id.button);
        btnStart = findViewById(R.id.button1);
        tvResult.setText("等待場景建立中...");
        btnStart.setText("錄音");
        btnStop.setOnClickListener(v -> {
            robotAPI.robot.stopSpeak();
        });


        charSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                character = isChecked ? "1" : "0";
                if (character.equals("1")) charSwitch.setText("PC first");
                else charSwitch.setText("User first");
            }
        });
        instruction_New_Environment = String.format(
                " create a new,realistic conversation scenario with a specific purpose .The topic must be related to %s .The scenario could be something like visiting a restaurant, making a phone call, or attending a job interview.  \"" +
                        " Each time, the generated scenario should be different from the previous and have a distinct purpose." +
                        " Only generate one scenario and purpose in one time. " +
                        " For example:" +
                        " 場景: Airport customs\n目的: The traveler needs to go through customs after arriving in a new country.\n" +
                        " 場景: Library Visit\n目的: The customer is asking for help finding a book at the library.\n" +
                        " 場景: Hotel Check-in\n目的: The guest needs to check into their hotel room after arriving.\n" +
                        " 場景: Grocery Store Shopping\n目的: The shopper is looking for a specific item in the grocery store.\n" +
                        " 場景: Job Interview\n目的: The candidate is attending a job interview and answering questions from the interviewer.\n" +
                        " 場景: Airport Security\n目的: The traveler needs to go through airport security before boarding their flight.\n" +
                        " 場景: Doctor's Appointment\n目的: The patient is explaining their symptoms to the doctor during a consultation.\n" +
                        " 場景: Restaurant Ordering\n目的: The customer is ordering food at a restaurant and asking about the menu options.\n" +
                        " 場景: Public Transportation Ticket Purchase\n目的: The passenger is buying a ticket for public transportation at the counter.\n" +
                        " 場景: Bank Visit\n目的: The customer is making a deposit and inquiring about their account balance at the bank.\n" +
                        " 場景: Post Office Visit\n目的: The customer is sending a package and asking about international shipping options.\n" +
                        " 場景: Fitness Center Inquiry\n目的: The individual is asking about membership options and gym facilities at a fitness center.\n" +
                        " 場景: Movie Ticket Purchase\n目的: The person is buying tickets for a movie and asking about showtimes.\n" +
                        " 場景: Pharmacy Visit\n目的: The customer is asking for advice about over-the-counter medication.\n" +
                        " 場景: Car Rental\n目的: The customer is renting a car and asking for information about the rental agreement and insurance options.\n"
                , preferType);
        String instruction_evualuate = (
                "我會輸入一段對話。請幫我根據下方的評價方法為對話評分" +
                        "The evaluation format should look like[Fluency: score, Accuracy: score, Relevance: score, Lexical Richness: score, Tone and Politeness: score, Engagement: score]\n" +
                        "評價方法:" +
                        "Fluency (流暢性)：\n" +
                        "\n" +
                        "評價標準：檢查語句是否通順，語法是否正確，是否有冗長或過於簡單的部分。\n" +
                        "5分：語句非常流暢，語法無誤，聽起來非常自然。\n" +
                        "4分：語句流暢，少數語法或用詞上有微小問題。\n" +
                        "3分：語句結構正常，但略顯笨拙，偶有語法錯誤。\n" +
                        "2分：語句不流暢，有語法錯誤且不自然。\n" +
                        "1分：語句不通順，語法錯誤明顯，理解困難。\n" +
                        "Accuracy (準確性)：\n" +
                        "\n" +
                        "評價標準：回應的內容是否準確，符合背景或問題的要求。\n" +
                        "5分：完全準確，信息無誤。\n" +
                        "4分：大致準確，僅有細微誤差。\n" +
                        "3分：有些小錯誤，但整體意思仍可理解。\n" +
                        "2分：準確性有問題，可能會造成理解上的誤解。\n" +
                        "1分：回應完全不準確，與問題無關。\n" +
                        "Relevance (相關性)：\n" +
                        "\n" +
                        "評價標準：回應是否直接回答問題或符合背景需求，是否偏離主題。\n" +
                        "5分：完全相關，直截了當地回答了問題或跟背景緊密相關。\n" +
                        "4分：大部分相關，但有一些附加的、不必要的內容。\n" +
                        "3分：部分相關，可能偏離了問題或上下文的主題。\n" +
                        "2分：回應與問題或背景無關，較為混亂。\n" +
                        "1分：完全不相關，完全未能回答問題。\n" +
                        "Lexical Richness (詞彙豐富性)：\n" +
                        "\n" +
                        "評價標準：評價回應中使用的詞彙是否豐富，避免過於重複的詞語，是否有簡單或過度平鋪直敘的表達。\n" +
                        "5分：詞彙豐富，表達清晰多樣，避免重複。\n" +
                        "4分：詞彙豐富，偶爾有輕微重複或簡單的表達。\n" +
                        "3分：詞彙普通，表達有些單調或重複。\n" +
                        "2分：詞彙貧乏，表達單一且重複。\n" +
                        "1分：詞彙極其簡單，表達單調且重複。\n" +
                        "Tone and Politeness (語氣和禮貌性)：\n" +
                        "\n" +
                        "評價標準：語氣是否得當，是否符合場合的禮貌要求，回應是否尊重且友好。\n" +
                        "5分：語氣友好，禮貌，完全符合情境。\n" +
                        "4分：語氣合適，略有改進空間，但仍然禮貌。\n" +
                        "3分：語氣基本合適，但不夠自然或稍顯生硬。\n" +
                        "2分：語氣不夠禮貌或缺乏情感。\n" +
                        "1分：語氣粗魯，或未能表達適當的禮貌。\n" +
                        "Engagement (互動性)：\n" +
                        "\n" +
                        "評價標準：回應是否鼓勵進一步的對話或互動，是否有促進交流的元素。\n" +
                        "5分：積極鼓勵對話，能激發後續互動。\n" +
                        "4分：有一定的互動性，但有些部分缺乏引導性。\n" +
                        "3分：回應有些沉悶，沒有激發進一步的問題或對話。\n" +
                        "2分：回應缺乏互動性，沒有引導進一步交流。\n" +
                        "1分：回應完全不具互動性，沒有促進任何對話。" +
                        "以下是評語範例:(回應的評語請用中文)" +
                        "[流暢度：4，準確性：5，相關性：4，詞彙豐富度：3，語氣和禮貌：5，互動性：3]\n" +
                        "- 流暢度：改善措辭以提升表達的順暢性。\n" +
                        "- 準確性：準確無誤，表現良好。\n" +
                        "- 相關性：更多聚焦於核心細節。\n" +
                        "- 詞彙豐富度：使用更多具描述性的詞語。\n" +
                        "- 語氣和禮貌：友善且禮貌，保持下去。\n" +
                        "- 互動性：加入開放式問題以提升互動性。\n" +
                        "如果無輸入,則回覆\"未開始對話\""
        );
//        String instruction1 = String.format(
//                "  Your name is Zenbo, you are an English conversationalist, use English letters to answer, use chinese to explain.\n" +
//                        "  When I initiate a conversation, respond with simply and shortly like a regular person, based on the context of the conversation.\n" +
//                        "  For example:\n" +
//                        "  roleA: 'What can I eat at McDonald's?'\n" +
//                        "  roleB: 'You can choose from the current popular items and add a combo meal.'\n" +
//                        "\n" +
//                        "  -If your response deviates from the expected context, you will reply with \"Please say it again.\"\n" +
//                        "  Only provide answers related to the intended topic.\n" +
//                        "  You are acting as a normal human.Please respond in English with a conversational tone.\n" +
//                        "\n" +
//                        "  At the end of every conversation, you will strictly evaluate the user reply based on six criteria:\n" +
//                        "    \"Fluency, Accuracy, Relevance, Lexical Richness, Tone and Politeness, and Engagement\"\n" +
//                        "  Provide a score between 1 and 5 for each criterion, followed by a brief but rigorous explanation. Be strict and meticulous in your evaluation, ensuring a thorough assessment of each response.\n" +
//                        "  The evaluation format should look like[Fluency: score, Accuracy: score, Relevance: score, Lexical Richness: score, Tone and Politeness: score, Engagement: score]\n" +
//                        "\n" +
//                        "  When the user inputs \"generate environment\", create a new,realistic conversation scenario with a specific purpose.The scenario could be something like visiting a restaurant, making a phone call, or attending a job interview.  " +
//                        "  Each time, the generated scenario should be different from the previous and have a distinct purpose." +
//                        "  After generating the scene show the purpose of the interaction. If \"%s\" is \"配角\", you will start chat after generating the scene." +
//                        "  For example, after \"generate environment\", you might create a scenario like:\n" +
//                        "    Scenario: Airport customs\n" +
//                        "    Purpose: The traveler needs to go through customs after arriving in a new country.\n" +
//                        "    or" +
//                        "    Scenario:  Library Visit\n" +
//                        "    Purpose: The customer is asking for help finding a book at the library.\n" +
//                        "\n" +
//                        "  When the user inputs 'Start chat', respond accordingly based on the conversation.\n" +
//                        "  Then you will then proceed to carry on the conversation in accordance with the generated environment and its purpose.", charSwitch.getTextOn()
//        );

        String role = "";
        String instruction = String.format(
                "  You are an English conversation Robot, use simple English to reply shortly, use traditional chinese to explain. You will play the role talk with users.\n" +

                        "The following are examples of a character's personality:" +
                        "[1]" +
                        "Response style: Enthusiastic, friendly, and eager to help.\n" +
                        "Tone: Cheerful and warm, using casual language to make the customer feel at ease.\n" +
                        "Example response: 'Oh, you’re in for a treat! Let me show you some of our best-sellers! You’ll love these, they’re super popular right now!'\n" +

                        "[2]" +
                        "Response style: Professional, assertive, and focused on results.\n" +
                        "Tone: Direct, confident, and no-nonsense, with an emphasis on efficiency and making decisions.\n" +
                        "Example response: 'We have a strong proposal that aligns with your goals. Let’s focus on the numbers and see how we can move forward with a partnership.'\n" +

                        "[3]" +
                        "Response style: Upbeat, fun, and welcoming.\n" +
                        "Tone: Light-hearted and energetic, engaging with the customer in a relaxed, conversational manner.\n" +
                        "Example response: 'I’d totally recommend the caramel macchiato, it’s one of our fan favorites! It’s got the perfect balance of sweet and bold.'\n" +

                        "[4]" +
                        "Response style: Patient, kind, and eager to assist.\n" +
                        "Tone: Calm, polite, and informative, ensuring the visitor feels guided and supported.\n" +
                        "Example response: 'Of course, I’d be happy to help! The book you’re looking for is in the fiction section, aisle 3, just past the mystery books. Let me know if you need anything else!'\n" +

                        "[5]" +
                        "Response style: Knowledgeable, confident, and friendly.\n" +
                        "Tone: Excited and informative, sharing recommendations with enthusiasm and authority.\n" +
                        "Example response: 'You absolutely have to visit the museum, it’s an iconic spot! You can also take a scenic walk around the park, it’s beautiful at sunset!'\n" +

                        "[6]" +
                        "Response style: Casual, witty, and a little sarcastic.\n" +
                        "Tone: Playful and a bit cheeky, using humor and slight sarcasm to respond.\n" +
                        "Example response: 'Oh, you need help with that? No problem, I guess I can spare a moment. It’s like the hardest task ever, right?' (said with a grin)'\n" +

                        "[7]" +
                        "Response style: Caring, patient, and understanding.\n" +
                        "Tone: Soothing, gentle, and empathetic, offering support without judgment.\n" +
                        "Example response: 'I understand that you’re feeling overwhelmed right now. It’s okay to take things one step at a time. You’re not alone in this.'\n" +

                        "[8]" +
                        "Response style: Innocent, inquisitive, and playful.\n" +
                        "Tone: Bright and curious, asking questions that reflect a genuine desire to learn.\n" +
                        "Example response: 'Why is the sky blue? How do clouds stay up there? I want to know EVERYTHING!'\n" +

                        "  For example:\n" +
                        "  roleA: 'What can I eat at McDonald's?'\n" +
                        "  roleB: 'You can choose from the current popular items and add a combo meal.'\n" +
                        "\n" +
                        "  -If your response deviates from the expected context, you will reply with \"Please say it again.\"\n" +
                        "  Only provide answers related to the intended topic.\n" +
                        "  When the user inputs \"Get environment\", Use the received reply as the current scenario and initiate a new conversation." +
                        "  If the user inputs 'Start chat', respond accordingly based on the conversation.else waiting for user input\n" +
                        "\n" +
                        "  Then you will then proceed to carry on the conversation in accordance with the generated environment and its purpose.", role, charSwitch.getText()
        );

        history_evu.add(instruction_evualuate);
        history_env.add(instruction_New_Environment);
        history.add(instruction);

        getPrefer.setOnEditorActionListener((v, actionId, event) -> {
            // 檢查是否按下「Enter」或「完成」
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 取得輸入的文字
                preferType = getPrefer.getText().toString();
                history_env.clear();
                instruction_New_Environment = String.format(
                        " create a new,realistic conversation scenario with a specific purpose .The topic must be related to %s .The scenario could be something like visiting a restaurant, making a phone call, or attending a job interview.  \"" +
                                " Each time, the generated scenario should be different from the previous and have a distinct purpose." +
                                " Only generate one scenario and purpose in one time. " +
                                " For example:" +
                                " 場景: Airport customs\n目的: The traveler needs to go through customs after arriving in a new country.\n" +
                                " 場景: Library Visit\n目的: The customer is asking for help finding a book at the library.\n" +
                                " 場景: Hotel Check-in\n目的: The guest needs to check into their hotel room after arriving.\n" +
                                " 場景: Grocery Store Shopping\n目的: The shopper is looking for a specific item in the grocery store.\n" +
                                " 場景: Job Interview\n目的: The candidate is attending a job interview and answering questions from the interviewer.\n" +
                                " 場景: Airport Security\n目的: The traveler needs to go through airport security before boarding their flight.\n" +
                                " 場景: Doctor's Appointment\n目的: The patient is explaining their symptoms to the doctor during a consultation.\n" +
                                " 場景: Restaurant Ordering\n目的: The customer is ordering food at a restaurant and asking about the menu options.\n" +
                                " 場景: Public Transportation Ticket Purchase\n目的: The passenger is buying a ticket for public transportation at the counter.\n" +
                                " 場景: Bank Visit\n目的: The customer is making a deposit and inquiring about their account balance at the bank.\n" +
                                " 場景: Post Office Visit\n目的: The customer is sending a package and asking about international shipping options.\n" +
                                " 場景: Fitness Center Inquiry\n目的: The individual is asking about membership options and gym facilities at a fitness center.\n" +
                                " 場景: Movie Ticket Purchase\n目的: The person is buying tickets for a movie and asking about showtimes.\n" +
                                " 場景: Pharmacy Visit\n目的: The customer is asking for advice about over-the-counter medication.\n" +
                                " 場景: Car Rental\n目的: The customer is renting a car and asking for information about the rental agreement and insurance options.\n"
                        , preferType);
                history_env.add(instruction_New_Environment);
                Log.d(LOG, "getPrefer: " + instruction_New_Environment);
                tvResult.setText("題目指定: " + preferType);
                // 隱藏鍵盤
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true; // 返回 true 表示事件已處理
            }
            return false; // 返回 false 表示事件未處理
        });

        btnNew.setOnClickListener(v -> {
            robotAPI.robot.stopSpeak();
            tvResult.setText("等待場景建立中...");
            sendMessage_ENV(Environment);
        });
        sendMessage_ENV(Environment);

        btnStart.setOnClickListener(v -> {
            robotAPI.robot.stopSpeak();
            robotAPI.robot.speakAndListen("start", new SpeakConfig().timeout(20).listenLanguageId(2));  //設定語言為英文
        });

        btnEvualuate.setOnClickListener(v -> {
            robotAPI.robot.stopSpeak();
            sendMessage_EVU(evaluate);
        });


    }


    private static void sendMessage_ENV(GenerativeModelFutures model) {
        conversation.clear();
        // 將歷史訊息拼接成單一內容
        StringBuilder combinedHistory = new StringBuilder();
        for (String message : history_env) {
            combinedHistory.append(message).append("\n");
        }
        combinedHistory.append("generate environment");

        // 更新歷史訊息
        history_env.add("generate environment");

        // 新建一個內容物件用於 API 請求
        Content combinedContent = new Content.Builder()
                .addText(combinedHistory.toString())
                .build();

        Executor executor = Executors.newSingleThreadExecutor();

        // 發送拼接後的對話歷史內容
        ListenableFuture<GenerateContentResponse> response = Environment.generateContent(combinedContent);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
//                int index_of_star = resultText.indexOf('*'),conut_of_star = 0;
//                while (true) {
//                    if(index_of_star==-1)break;
//                    else {
//                        conut_of_star++;
//
//                    }
//                }
                int index = resultText.indexOf("**Environment:**");
                if (index != -1) {
                    resultText = resultText.substring(0, index);  // 截取到子字串 "World" 前的位置
                    Log.d(LOG, "get Env success: " + resultText);
                } else {
                    Log.d(LOG, "get Env fail: ");
                    index = resultText.indexOf("**Conversation:**");
                    if (index != -1) {
                        resultText = resultText.substring(0, index);  // 截取到子字串 "World" 前的位置
                        Log.d(LOG, "get Con success: " + resultText);
                    } else {
                        Log.d(LOG, "get Con fail: ");
                        index = resultText.indexOf("**Scene:");
                        if (index != -1) {
                            resultText = resultText.substring(0, index);  // 截取到子字串 "World" 前的位置
                            Log.d(LOG, "get Scene success: " + resultText);
                        } else Log.d(LOG, "get Scene fail: ");
                    }
                }

                tvResult.setText(resultText);
                conversation.add(resultText);
                sendMessage("Get environment" + resultText, model);
                // 將模型回應加入歷史紀錄中
                history_env.add(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
//                Log.d(LOG, "get text fail: " + t.toString());
                t.printStackTrace();
            }
        }, executor);
    }

    private static void sendMessage_EVU(GenerativeModelFutures model) {

        // 將歷史訊息拼接成單一內容
        StringBuilder combinedHistory = new StringBuilder();
        combinedHistory.append(history_evu);
        combinedHistory.append(conversation);
        conversation.clear();
        // 新建一個內容物件用於 API 請求
        Content combinedContent = new Content.Builder()
                .addText(combinedHistory.toString())
                .build();

        Executor executor = Executors.newSingleThreadExecutor();

        // 發送拼接後的對話歷史內容
        ListenableFuture<GenerateContentResponse> response = evaluate.generateContent(combinedContent);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                tvResult.setText(resultText);
                // 將模型回應加入歷史紀錄中

            }

            @Override
            public void onFailure(Throwable t) {
//                Log.d(LOG, "get text fail: " + t.toString());
                t.printStackTrace();
            }
        }, executor);
    }

    private static void sendMessage(String userMessageText, GenerativeModelFutures model) {
        // 將歷史訊息拼接成單一內容
        StringBuilder combinedHistory = new StringBuilder();
        for (String message : history) {
            combinedHistory.append(message).append("\n");
        }
        combinedHistory.append("User: ").append(userMessageText);

        // 更新歷史訊息
        history.add("User: " + userMessageText);

        // 新建一個內容物件用於 API 請求
        Content combinedContent = new Content.Builder()
                .addText(combinedHistory.toString())
                .build();
        if (userMessageText.contains("Get environment")) {
            Log.d(LOG, "get environment");
            if (character.equals("1")) {

                Executor executor = Executors.newSingleThreadExecutor();

                // 發送拼接後的對話歷史內容
                ListenableFuture<GenerateContentResponse> response = model.generateContent(combinedContent);
                Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        sendMessageForStart(model);
                    }

                    @Override
                    public void onFailure(Throwable t) {
//                Log.d(LOG, "get text fail: " + t.toString());
                        t.printStackTrace();
                    }
                }, executor);
            }
        } else {
            Executor executor = Executors.newSingleThreadExecutor();

            // 發送拼接後的對話歷史內容
            ListenableFuture<GenerateContentResponse> response = model.generateContent(combinedContent);
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText();
//                Log.d(LOG, "get text success: " + resultText);
//                runOnUiThread(() -> {

                    tvResult.append("\n" + resultText);
                    conversation.add(resultText);
                    robotAPI.robot.stopSpeak();
                    String clearResultText;

                    clearResultText = resultText;  // 若沒有 "["，返回原字串

                    SpeakConfig config1 = new SpeakConfig();
                    config1.speed(85);
                    config1.listenLanguageId(2);
                    config1.languageId(2);
                    String resultForSpeak = clearResultText.replaceAll("[()':;。，、；：「」？！]", "");
                    resultForSpeak = resultForSpeak.replaceAll("[\\u4E00-\\u9FFF]","");
                    Log.d(LOG, "Response Text displayed: " + resultForSpeak);
                    robotAPI.robot.speak(resultForSpeak, config1);
//                    Log.d(LOG, "Response Text displayed: " + resultText);
//                });

                    // 將模型回應加入歷史紀錄中
                    history.add("Model: " + resultText);

                }

                @Override
                public void onFailure(Throwable t) {
//                Log.d(LOG, "get text fail: " + t.toString());
                    t.printStackTrace();
                }
            }, executor);
        }
    }

    private static void sendMessageForStart(GenerativeModelFutures model) {
        // 將歷史訊息拼接成單一內容
        StringBuilder combinedHistory = new StringBuilder();
        for (String message : history) {
            combinedHistory.append(message).append("\n");
        }
        combinedHistory.append("User: ").append("Start Chat");

        // 新建一個內容物件用於 API 請求
        Content combinedContent = new Content.Builder()
                .addText(combinedHistory.toString())
                .build();

        Executor executor = Executors.newSingleThreadExecutor();

        // 發送拼接後的對話歷史內容
        ListenableFuture<GenerateContentResponse> response = model.generateContent(combinedContent);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
//                Log.d(LOG, "get text success: " + resultText);
//                runOnUiThread(() -> {

                tvResult.append("\n" + resultText);
                conversation.add(resultText);
                robotAPI.robot.stopSpeak();
                String clearResultText;

                clearResultText = resultText;  // 若沒有 "["，返回原字串

                SpeakConfig config1 = new SpeakConfig();
                config1.speed(85);
                config1.listenLanguageId(2);
                config1.languageId(2);
                String resultForSpeak = clearResultText.replaceAll("[()':;。，、；：「」？！]", "");
                resultForSpeak = resultForSpeak.replaceAll("[\\u4E00-\\u9FFF]","");
                Log.d(LOG, "Response Text displayed: " + resultForSpeak);
                robotAPI.robot.speak(resultForSpeak, config1);
//                    Log.d(LOG, "Response Text displayed: " + resultText);
//                });

                // 將模型回應加入歷史紀錄中
                history.add("Model: " + resultText);

            }

            @Override
            public void onFailure(Throwable t) {
//                Log.d(LOG, "get text fail: " + t.toString());
                t.printStackTrace();
            }
        }, executor);

    }


//    private static void sendMessage(String userMessageText, GenerativeModelFutures model) {
//
//        //多輪對話寫法
//        //Create previous chat rule for context
//        Content.Builder userContentBuilder = new Content.Builder();
//        userContentBuilder.setRole("user");
//        userContentBuilder.addText("How much does the bag cost?");
//        Content userContent = userContentBuilder.build();
//
//        Content.Builder modelContentBuilder = new Content.Builder();
//        modelContentBuilder.setRole("model");
//        modelContentBuilder.addText("You are an English conversation chatbot, responsible for providing scenarios for conversations, such as at customs, asking for directions, or making a payment. You will only respond in English.");
//        Content modelContent = modelContentBuilder.build();
//
//        List<Content> history = Arrays.asList(userContent, modelContent);
//        // Initialize the chat
//        ChatFutures chat = model.startChat(history);
//
//        //Create a new user message
//        Content.Builder userMessageBuilder = new Content.Builder();
//        userMessageBuilder.setRole("user");
//        userMessageBuilder.addText(userMessageText);
//        Content userMessage = userMessageBuilder.build();
//
//
//        //注意
//        Executor executor = Executors.newSingleThreadExecutor();
//
//        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(userMessage);
//        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
//            @Override
//            public void onSuccess(GenerateContentResponse result) {
//                String resultText = result.getText();
//                inputStr = resultText;
//                setRobotExpression(RobotFace.HAPPY, inputStr);
//                mCountDownTimer.start();
//                tvResult.setText(inputStr);
//
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                tvResult.setText("Error: AI generation failed");
//                t.printStackTrace();
//            }
//        }, executor);
//    }


    public static String extractData(String input) {
        // 正则表达式匹配模式
        String regex = "\\\\\"result\\\\\":\\s*\\[\\\\\"(.*?)\\\\\"\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        // 查找并返回匹配的内容
        if (matcher.find()) {
            return matcher.group(1); // 提取捕获组中的内容
        }

        return null; // 如果没有找到
    }


    // 申請權限
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);
        }

        @Override
        public void initComplete() {
            super.initComplete();
        }
    };


    public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
        @Override
        public void onFinishRegister() {
        }

        @Override
        public void onVoiceDetect(JSONObject jsonObject) {

        }

        @Override
        public void onSpeakComplete(String s, String s1) {

        }

        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {
            clearData = extractData(jsonObject.toString());
            if (clearData != null) {
                tvResult.append("\nUser:　" + clearData);
                inputStr = clearData;
//                wordCount = inputStr.split("\\s+").length;
                sendMessage(clearData, model);
            } else {
                tvResult.append("\nNo Data");
            }

        }

        @Override
        public void onResult(JSONObject jsonObject) {

        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    // zenbo視窗設定
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }

    // 設置表情或說話或細節的函式
    private static void setRobotExpression(RobotFace face) {
        robotAPI.robot.setExpression(face);
    }

    private static void setRobotExpression(RobotFace face, String text) {
        ExpressionConfig config = new ExpressionConfig();
        config.speed(85);
        robotAPI.robot.setExpression(face, text, config);
    }

    private static void setRobotExpression(RobotFace face, String text, ExpressionConfig config) {
        robotAPI.robot.setExpression(face, text, config);
    }

    // handle StateIntentions LanguageType
    private static JSONObject handleLanguageType(String language) {
        JSONObject LanguageType = new JSONObject();

        try {
            LanguageType.put("LanguageType", language);
        } catch (JSONException jex) {
            jex.printStackTrace();
        }
        return LanguageType;
    }


    public MainActivity() {
        super(robotCallback, robotListenCallback);
    }
}



