package com.autoforge.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(14, 15, 18);
    private static final int PANEL = Color.rgb(28, 29, 34);
    private static final int RED = Color.rgb(229, 57, 53);
    private static final int WHITE = Color.rgb(248, 248, 250);
    private static final int MUTED = Color.rgb(174, 176, 187);
    private static final int REQUEST_EXPORT = 214;
    private static final String MODEL = "gemini-2.5-flash";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<String, String> currentFiles = new LinkedHashMap<>();
    private EditText apiKeyInput;
    private EditText promptInput;
    private TextView statusText;
    private TextView projectTitle;
    private LinearLayout fileList;
    private Button generateButton;
    private Button previewButton;
    private Button exportButton;
    private String currentProjectName = "";
    private File currentProjectDir;
    private File pendingZip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        showHome();
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private void showHome() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(28));
        scroll.addView(root);
        setContentView(scroll);

        LinearLayout brand = new LinearLayout(this);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        TextView mark = text("AF", 19, WHITE, true);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(round(RED, 14));
        brand.addView(mark, new LinearLayout.LayoutParams(dp(52), dp(52)));
        LinearLayout brandWords = new LinearLayout(this);
        brandWords.setOrientation(LinearLayout.VERTICAL);
        brandWords.setPadding(dp(12), 0, 0, 0);
        brandWords.addView(text("AUTOFORGE", 22, WHITE, true));
        brandWords.addView(text("من الوصف إلى ملفات المشروع", 13, MUTED, false));
        brand.addView(brandWords);
        root.addView(brand);
        gap(root, 24);

        root.addView(text("حوّل فكرتك إلى تطبيق", 28, WHITE, true));
        gap(root, 7);
        root.addView(text("صف فكرتك، وسيُنشئ AutoForge مشروع ويب أوليًا بملفات منفصلة يمكنك معاينتها وحفظها وتصديرها.", 15, MUTED, false));
        gap(root, 20);

        LinearLayout keyCard = panel();
        keyCard.addView(text("1. مفتاح Gemini API", 17, WHITE, true));
        gap(keyCard, 8);
        apiKeyInput = new EditText(this);
        apiKeyInput.setSingleLine(true);
        apiKeyInput.setTextSize(15);
        apiKeyInput.setTextColor(WHITE);
        apiKeyInput.setHintTextColor(MUTED);
        apiKeyInput.setHint("أدخل مفتاحك عند الاستخدام");
        apiKeyInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        apiKeyInput.setPadding(dp(12), dp(12), dp(12), dp(12));
        apiKeyInput.setBackground(round(Color.rgb(42, 43, 49), 10));
        keyCard.addView(apiKeyInput, matchWrap());
        gap(keyCard, 8);
        keyCard.addView(text("لا يُضمَّن المفتاح في ملفات المشروع ولا يُحفظ في سجل AutoForge. أدخله في كل جلسة، ولا تشارك مفتاحك مع أحد.", 12, MUTED, false));
        root.addView(keyCard);
        gap(root, 12);

        LinearLayout promptCard = panel();
        promptCard.addView(text("2. صف التطبيق المطلوب", 17, WHITE, true));
        gap(promptCard, 8);
        promptInput = new EditText(this);
        promptInput.setTextSize(15);
        promptInput.setTextColor(WHITE);
        promptInput.setHintTextColor(MUTED);
        promptInput.setHint("مثال: تطبيق لإدارة المصروفات، لإضافة العمليات وتصنيفها وعرض ملخص شهري...");
        promptInput.setMinLines(5);
        promptInput.setGravity(Gravity.TOP | Gravity.RIGHT);
        promptInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        promptInput.setPadding(dp(12), dp(12), dp(12), dp(12));
        promptInput.setBackground(round(Color.rgb(42, 43, 49), 10));
        promptCard.addView(promptInput, matchWrap());
        gap(promptCard, 8);
        promptCard.addView(text("النطاق الأول: تطبيق ويب متجاوب بملفات HTML وCSS وJavaScript، دون مكتبات خارجية إلزامية.", 12, MUTED, false));
        root.addView(promptCard);
        gap(root, 14);

        generateButton = button("إنشاء ملفات المشروع");
        generateButton.setOnClickListener(v -> generateProject());
        root.addView(generateButton, matchWrap());
        gap(root, 10);

        previewButton = button("معاينة المشروع");
        previewButton.setEnabled(false);
        previewButton.setOnClickListener(v -> previewProject());
        root.addView(previewButton, matchWrap());
        gap(root, 8);

        exportButton = button("تصدير المصدر كملف ZIP");
        exportButton.setEnabled(false);
        exportButton.setOnClickListener(v -> exportProject());
        root.addView(exportButton, matchWrap());
        gap(root, 8);

        Button savedButton = button("مشاريعي المحفوظة");
        savedButton.setOnClickListener(v -> showSavedProjects());
        root.addView(savedButton, matchWrap());
        gap(root, 14);

        statusText = text("جاهز. أنشئ أول مشروع لتظهر الملفات هنا.", 13, MUTED, false);
        statusText.setPadding(dp(3), dp(4), dp(3), dp(4));
        root.addView(statusText);
        gap(root, 12);

        LinearLayout filesCard = panel();
        projectTitle = text("ملفات المشروع", 18, WHITE, true);
        filesCard.addView(projectTitle);
        gap(filesCard, 8);
        fileList = new LinearLayout(this);
        fileList.setOrientation(LinearLayout.VERTICAL);
        filesCard.addView(fileList);
        root.addView(filesCard);
        gap(root, 20);

        TextView footer = text("AUTOFORGE • نواة أولية • 0.2.0", 11, MUTED, false);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer);
    }

    private void generateProject() {
        final String apiKey = apiKeyInput.getText().toString().trim();
        final String userPrompt = promptInput.getText().toString().trim();
        if (apiKey.isEmpty()) {
            apiKeyInput.setError("أدخل مفتاح Gemini API");
            return;
        }
        if (userPrompt.length() < 12) {
            promptInput.setError("أضف تفاصيل أكثر عن التطبيق المطلوب");
            return;
        }

        setBusy(true, "يُرسل الوصف إلى Gemini ويُنشئ ملفات المشروع...");
        executor.execute(() -> {
            try {
                JSONObject result = generateWithGemini(apiKey, userPrompt);
                String proposedName = result.optString("projectName", "AutoForge Project").trim();
                final String name = proposedName.isEmpty() ? "AutoForge Project" : proposedName;
                LinkedHashMap<String, String> generated = validateGeneratedFiles(result.getJSONArray("files"));
                File savedDir = saveProject(name, userPrompt, generated);
                runOnUiThread(() -> {
                    currentProjectName = name;
                    currentProjectDir = savedDir;
                    currentFiles.clear();
                    currentFiles.putAll(generated);
                    refreshFileList();
                    previewButton.setEnabled(currentFiles.containsKey("index.html"));
                    exportButton.setEnabled(!currentFiles.isEmpty());
                    setBusy(false, "اكتمل التوليد وحُفظ المشروع على الجهاز. راجع الملفات قبل التصدير.");
                });
            } catch (Exception e) {
                String message = e.getMessage();
                if (message == null || message.trim().isEmpty()) message = "حدث خطأ غير معروف.";
                final String error = message;
                runOnUiThread(() -> setBusy(false, "تعذّر إنشاء المشروع: " + error));
            }
        });
    }

    private JSONObject generateWithGemini(String apiKey, String userPrompt) throws Exception {
        JSONObject request = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        JSONArray parts = new JSONArray();
        String instruction =
                "You are AutoForge, a careful web-app scaffolding engine. " +
                "Return only a JSON object matching the supplied schema. " +
                "Build a useful, working, mobile-first web app from the user's idea. " +
                "Include index.html, styles.css, app.js, and README.md at the project root. " +
                "You may add up to 10 small supporting files. Do not use external CDNs, remote libraries, secrets, or placeholder-only buttons. " +
                "Implement the core workflow with plain HTML, CSS, and JavaScript. Use localStorage only when suitable. " +
                "Make the interface accessible, responsive, and visually coherent. Keep each file concise. " +
                "The index.html must link to styles.css and app.js using relative paths. " +
                "Use the user's language for visible UI text when identifiable. Do not add Markdown fences. " +
                "App description:\n" + userPrompt;
        parts.put(new JSONObject().put("text", instruction));
        message.put("parts", parts);
        contents.put(message);
        request.put("contents", contents);

        JSONObject config = new JSONObject();
        config.put("temperature", 0.25);
        config.put("maxOutputTokens", 8192);
        config.put("responseMimeType", "application/json");

        JSONObject responseSchema = new JSONObject();
        responseSchema.put("type", "OBJECT");
        JSONObject properties = new JSONObject();
        properties.put("projectName", new JSONObject().put("type", "STRING"));
        JSONObject fileItem = new JSONObject();
        fileItem.put("type", "OBJECT");
        JSONObject itemProperties = new JSONObject();
        itemProperties.put("path", new JSONObject().put("type", "STRING"));
        itemProperties.put("content", new JSONObject().put("type", "STRING"));
        fileItem.put("properties", itemProperties);
        JSONArray requiredItemFields = new JSONArray();
        requiredItemFields.put("path");
        requiredItemFields.put("content");
        fileItem.put("required", requiredItemFields);
        JSONObject filesSchema = new JSONObject();
        filesSchema.put("type", "ARRAY");
        filesSchema.put("items", fileItem);
        properties.put("files", filesSchema);
        responseSchema.put("properties", properties);
        JSONArray requiredFields = new JSONArray();
        requiredFields.put("projectName");
        requiredFields.put("files");
        responseSchema.put("required", requiredFields);
        config.put("responseSchema", responseSchema);
        request.put("generationConfig", config);

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(120000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("x-goog-api-key", apiKey);
            byte[] payload = request.toString().getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(payload.length);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(payload);
            }

            int code = connection.getResponseCode();
            InputStream stream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
            String responseText = readAll(stream);
            if (code < 200 || code >= 300) {
                String detail = "رمز HTTP " + code;
                try {
                    JSONObject error = new JSONObject(responseText).optJSONObject("error");
                    if (error != null) {
                        String apiMessage = error.optString("message", "");
                        if (!apiMessage.isEmpty()) detail += " — " + apiMessage;
                    }
                } catch (JSONException ignored) {}
                if (code == 401 || code == 403) detail += " (تحقق من صلاحية المفتاح والمشروع).";
                if (code == 429) detail += " (قد تكون الحصة قد انتهت؛ راجع حدود الاستخدام في AI Studio).";
                throw new Exception(detail);
            }

            JSONObject outer = new JSONObject(responseText);
            JSONArray candidates = outer.optJSONArray("candidates");
            if (candidates == null || candidates.length() == 0) throw new Exception("لم يرجع النموذج نتيجة. جرّب وصفًا أقصر.");
            JSONObject responseContent = candidates.getJSONObject(0).optJSONObject("content");
            if (responseContent == null) throw new Exception("استجابة النموذج غير مكتملة.");
            JSONArray responseParts = responseContent.optJSONArray("parts");
            if (responseParts == null) throw new Exception("لا توجد ملفات في الاستجابة.");
            StringBuilder generatedText = new StringBuilder();
            for (int i = 0; i < responseParts.length(); i++) {
                generatedText.append(responseParts.getJSONObject(i).optString("text", ""));
            }
            String json = generatedText.toString().trim();
            int first = json.indexOf('{');
            int last = json.lastIndexOf('}');
            if (first >= 0 && last > first) json = json.substring(first, last + 1);
            return new JSONObject(json);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private LinkedHashMap<String, String> validateGeneratedFiles(JSONArray input) throws Exception {
        if (input.length() < 3 || input.length() > 15) throw new Exception("عدد الملفات غير مناسب. أعد المحاولة بوصف أبسط.");
        LinkedHashMap<String, String> safeFiles = new LinkedHashMap<>();
        for (int i = 0; i < input.length(); i++) {
            JSONObject item = input.getJSONObject(i);
            String path = item.optString("path", "").trim().replace('\\', '/');
            String body = item.optString("content", "");
            if (path.isEmpty() || path.startsWith("/") || path.contains("../") || path.equals("..") || path.contains("/..")) {
                throw new Exception("أعاد النموذج مسار ملف غير آمن؛ أعد المحاولة.");
            }
            if (!(path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js") ||
                    path.endsWith(".json") || path.endsWith(".md") || path.endsWith(".svg") || path.endsWith(".txt"))) {
                throw new Exception("نوع الملف غير مدعوم في النواة الحالية: " + path);
            }
            if (body.length() > 250000) throw new Exception("أحد الملفات أكبر من الحد المسموح.");
            if (safeFiles.containsKey(path)) throw new Exception("اسم ملف مكرر: " + path);
            safeFiles.put(path, body);
        }
        if (!safeFiles.containsKey("index.html")) throw new Exception("لم يُنشئ النموذج index.html المطلوب للمعاينة.");
        if (!safeFiles.containsKey("styles.css")) safeFiles.put("styles.css", "/* Project styles */");
        if (!safeFiles.containsKey("app.js")) safeFiles.put("app.js", "// Project behavior\n");
        if (!safeFiles.containsKey("README.md")) safeFiles.put("README.md", "# AutoForge Project\n\nGenerated by AutoForge.\n");
        return safeFiles;
    }

    private File saveProject(String name, String prompt, Map<String, String> files) throws Exception {
        File root = new File(getFilesDir(), "projects");
        if (!root.exists() && !root.mkdirs()) throw new Exception("تعذر إنشاء مجلد المشاريع.");
        String slug = name.toLowerCase().replaceAll("[^a-z0-9\\u0600-\\u06FF]+", "-");
        if (slug.length() > 32) slug = slug.substring(0, 32);
        if (slug.isEmpty()) slug = "project";
        File directory = new File(root, slug + "-" + System.currentTimeMillis());
        if (!directory.mkdirs()) throw new Exception("تعذر حفظ ملفات المشروع.");

        JSONArray paths = new JSONArray();
        for (Map.Entry<String, String> entry : files.entrySet()) {
            File output = safeChild(directory, entry.getKey());
            File parent = output.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) throw new Exception("تعذر إنشاء مجلد فرعي.");
            try (FileOutputStream stream = new FileOutputStream(output)) {
                stream.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
            }
            paths.put(entry.getKey());
        }
        JSONObject metadata = new JSONObject();
        metadata.put("projectName", name);
        metadata.put("createdAt", System.currentTimeMillis());
        metadata.put("paths", paths);
        metadata.put("prompt", prompt);
        try (FileOutputStream stream = new FileOutputStream(new File(directory, "project.json"))) {
            stream.write(metadata.toString(2).getBytes(StandardCharsets.UTF_8));
        }
        return directory;
    }

    private File safeChild(File root, String relativePath) throws Exception {
        File child = new File(root, relativePath);
        String base = root.getCanonicalPath() + File.separator;
        if (!child.getCanonicalPath().startsWith(base)) throw new Exception("مسار ملف خارج مجلد المشروع.");
        return child;
    }

    private void refreshFileList() {
        fileList.removeAllViews();
        projectTitle.setText("ملفات: " + currentProjectName);
        for (Map.Entry<String, String> entry : currentFiles.entrySet()) {
            Button item = button("▤  " + entry.getKey());
            item.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            item.setTextSize(14);
            item.setOnClickListener(v -> showFileContent(entry.getKey(), entry.getValue()));
            fileList.addView(item, matchWrap());
            gap(fileList, 6);
        }
        if (currentFiles.isEmpty()) fileList.addView(text("لم تُنشأ ملفات بعد.", 13, MUTED, false));
    }

    private void showFileContent(String name, String value) {
        ScrollView scroll = new ScrollView(this);
        TextView code = text(value, 12, WHITE, false);
        code.setTextIsSelectable(true);
        code.setTypeface(Typeface.MONOSPACE);
        code.setGravity(Gravity.LEFT | Gravity.TOP);
        code.setPadding(dp(12), dp(12), dp(12), dp(12));
        scroll.addView(code);
        new AlertDialog.Builder(this).setTitle(name).setView(scroll)
                .setPositiveButton("إغلاق", (d, w) -> {}).show();
    }

    private void previewProject() {
        String html = currentFiles.get("index.html");
        if (html == null) {
            Toast.makeText(this, "لا يوجد index.html للمعاينة.", Toast.LENGTH_LONG).show();
            return;
        }
        String css = currentFiles.get("styles.css");
        String js = currentFiles.get("app.js");
        if (css == null) css = "";
        if (js == null) js = "";
        html = html.replaceAll("(?is)<link[^>]*href=[\"']styles\\.css[\"'][^>]*>", "");
        html = html.replaceAll("(?is)<script[^>]*src=[\"']app\\.js[\"'][^>]*>\\s*</script>", "");
        String style = "<style>\n" + css + "\n</style>";
        String script = "<script>\n" + js + "\n</script>";
        if (html.toLowerCase().contains("</head>")) {
            html = html.replaceFirst("(?i)</head>", java.util.regex.Matcher.quoteReplacement(style + "</head>"));
        } else {
            html = style + html;
        }
        if (html.toLowerCase().contains("</body>")) {
            html = html.replaceFirst("(?i)</body>", java.util.regex.Matcher.quoteReplacement(script + "</body>"));
        } else {
            html += script;
        }

        WebView webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                return !"autoforge.local".equals(uri.getHost());
            }
        });
        webView.loadDataWithBaseURL("https://autoforge.local/", html, "text/html", "UTF-8", null);

        Dialog dialog = new Dialog(this);
        dialog.setContentView(webView);
        dialog.show();
        if (dialog.getWindow() != null) dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, dp(600));
    }

    private void exportProject() {
        if (currentFiles.isEmpty()) return;
        try {
            File dir = new File(getCacheDir(), "exports");
            if (!dir.exists() && !dir.mkdirs()) throw new Exception("تعذر إنشاء ملف التصدير.");
            pendingZip = new File(dir, "AutoForge-" + System.currentTimeMillis() + ".zip");
            try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(pendingZip))) {
                for (Map.Entry<String, String> entry : currentFiles.entrySet()) {
                    zip.putNextEntry(new ZipEntry(entry.getKey()));
                    zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                    zip.closeEntry();
                }
            }
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/zip");
            intent.putExtra(Intent.EXTRA_TITLE, safeExportName(currentProjectName) + ".zip");
            startActivityForResult(intent, REQUEST_EXPORT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "لا يتوفر مدير ملفات لحفظ ZIP.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "تعذر التصدير: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_EXPORT || resultCode != RESULT_OK || data == null || data.getData() == null || pendingZip == null) return;
        try (InputStream in = new FileInputStream(pendingZip);
             OutputStream out = getContentResolver().openOutputStream(data.getData())) {
            if (out == null) throw new Exception("لم يتم فتح وجهة الحفظ.");
            byte[] buffer = new byte[8192];
            int count;
            while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
            Toast.makeText(this, "تم تصدير ملفات المصدر إلى ZIP.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "تعذر حفظ الملف: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showSavedProjects() {
        File root = new File(getFilesDir(), "projects");
        File[] found = root.listFiles(File::isDirectory);
        if (found == null || found.length == 0) {
            new AlertDialog.Builder(this).setTitle("مشاريعي المحفوظة").setMessage("لا توجد مشاريع محفوظة حتى الآن.")
                    .setPositiveButton("حسنًا", (d, w) -> {}).show();
            return;
        }
        List<File> directories = new ArrayList<>();
        Collections.addAll(directories, found);
        Collections.sort(directories, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        List<File> valid = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (File dir : directories) {
            try {
                JSONObject meta = new JSONObject(readFile(new File(dir, "project.json")));
                valid.add(dir);
                labels.add(meta.optString("projectName", dir.getName()));
            } catch (Exception ignored) {}
        }
        if (valid.isEmpty()) {
            Toast.makeText(this, "لم أجد مشاريع قابلة للفتح.", Toast.LENGTH_LONG).show();
            return;
        }
        File[] choices = valid.toArray(new File[0]);
        new AlertDialog.Builder(this).setTitle("افتح مشروعًا محفوظًا")
                .setItems(labels.toArray(new String[0]), (d, which) -> loadSavedProject(choices[which]))
                .setNegativeButton("إلغاء", (d, w) -> {}).show();
    }

    private void loadSavedProject(File directory) {
        try {
            JSONObject meta = new JSONObject(readFile(new File(directory, "project.json")));
            JSONArray paths = meta.getJSONArray("paths");
            LinkedHashMap<String, String> loaded = new LinkedHashMap<>();
            for (int i = 0; i < paths.length(); i++) {
                String relative = paths.getString(i);
                loaded.put(relative, readFile(safeChild(directory, relative)));
            }
            currentFiles.clear();
            currentFiles.putAll(loaded);
            currentProjectName = meta.optString("projectName", directory.getName());
            currentProjectDir = directory;
            promptInput.setText(meta.optString("prompt", ""));
            refreshFileList();
            previewButton.setEnabled(currentFiles.containsKey("index.html"));
            exportButton.setEnabled(!currentFiles.isEmpty());
            setBusy(false, "تم فتح المشروع المحفوظ: " + currentProjectName);
        } catch (Exception e) {
            Toast.makeText(this, "تعذر فتح المشروع: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String readFile(File file) throws Exception {
        try (FileInputStream input = new FileInputStream(file);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            return output.toString("UTF-8");
        }
    }

    private String readAll(InputStream stream) throws Exception {
        if (stream == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) result.append(line).append('\n');
            return result.toString();
        }
    }

    private void setBusy(boolean busy, String message) {
        generateButton.setEnabled(!busy);
        apiKeyInput.setEnabled(!busy);
        promptInput.setEnabled(!busy);
        statusText.setText(message);
        statusText.setTextColor(busy ? RED : MUTED);
        generateButton.setText(busy ? "جارٍ إنشاء المشروع..." : "إنشاء ملفات المشروع");
    }

    private String safeExportName(String name) {
        String safe = name.replaceAll("[^A-Za-z0-9\\u0600-\\u06FF_-]+", "-");
        if (safe.length() > 45) safe = safe.substring(0, 45);
        return safe.isEmpty() ? "AutoForge-Project" : safe;
    }

    private LinearLayout panel() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(15), dp(15), dp(15), dp(15));
        box.setBackground(round(PANEL, 18));
        return box;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(WHITE);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(round(RED, 12));
        b.setPadding(dp(12), dp(9), dp(12), dp(9));
        return b;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.RIGHT);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setIncludeFontPadding(true);
        return t;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radius));
        return shape;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void gap(LinearLayout parent, int height) {
        View spacer = new View(this);
        parent.addView(spacer, new LinearLayout.LayoutParams(1, dp(height)));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
