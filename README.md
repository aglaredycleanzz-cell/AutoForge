# AutoForge

AutoForge is an AI-assisted app-generation workspace. The current MVP turns a natural-language description into an editable web-app project.

## Current MVP

- Arabic-first Android interface.
- Accepts a project description in Arabic or English.
- Calls the Gemini Developer API using a key entered at runtime.
- Generates source files including index.html, styles.css, app.js, and README.md.
- Shows generated source code inside the app.
- Previews the generated web app inside a WebView with local CSS and JavaScript embedded.
- Saves each generated project in app-private storage.
- Lists and reopens projects saved on the device.
- Exports source files as a ZIP archive using Android's save-file picker.
- Builds a debug APK through GitHub Actions.

## Scope boundary

The first MVP generates web-app source files. It does not yet compile arbitrary generated projects into standalone APKs, and it does not yet offer iterative code edits. Those are later stages after generation, preview, saving, and ZIP export are verified.

## Gemini API key and cost

The user enters their own Gemini API key when using the app. AutoForge does not hard-code, store, or include the key in exported projects. The app sends it in the request header over HTTPS.

A mobile client cannot provide the same secret protection as a secured server-side proxy. Before distributing a public production version, add a secured backend and abuse controls instead of using a shared API key inside an APK.

The Gemini API free tier has model-specific access and rate limits; availability may change and is not unlimited. Check the active quotas in Google AI Studio before relying on them.

Official references:
- Billing and free tier: https://ai.google.dev/gemini-api/docs/billing
- Rate limits: https://ai.google.dev/gemini-api/docs/rate-limits

## Build APK

Open Actions, select Build AutoForge Android APK, and manually run the workflow on the branch to test. Download the AutoForge-Android-APK artifact from that workflow run.
