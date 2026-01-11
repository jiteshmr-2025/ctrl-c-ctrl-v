# 📔 Smart Journal Application

An immersive, desktop-first journaling application developed for the **Fundamental of Programming (WIX 1002) Assignment** course at the **University of Malaya**. The "Smart Journal" bridges the gap between traditional self-reflection and modern data analytics by integrating real-time environmental context and AI-powered emotional insights.

## 🌟 Key Features

* **Secure Authentication**: Features a robust registration and login system.
* **Advanced Encryption**: Passwords are hashed using the SHA-256 algorithm with a unique salt to ensure data privacy.
* **Auto-Login Persistence**: Utilizes local session tokens (`session.token`) to bypass the login screen on subsequent launches.
* **Immersive Glassmorphism UI**: A high-fidelity interface featuring semi-transparent panes and dynamic background videos.
* **Atmospheric Replay**: Background visuals (e.g., rain, clouds, or clear skies) automatically change to match the current weather.
* **Automated Context Tracking**: Fetches real-time meteorological data for Malaysia via the **Malaysian Open Data API** whenever an entry is created.
* **AI Mood Analysis**: Processes journal entries through the **DistilBERT NLP model** via the Hugging Face API to provide mood labels and confidence scores.
* **Weekly Analytics Dashboard**: Aggregates data from the past 7 days into visual bar charts, allowing users to spot trends between environment and mood.
* **Hybrid Data Storage**: Employs a dual-storage model with local session management and **MongoDB Atlas Cloud** synchronization.

## 🛠️ Technologies Used

* **Language**: Java JDK 24
* **UI Framework**: JavaFX (Hardware-accelerated)
* **Build Tool**: Apache Maven
* **Database**: MongoDB Atlas (Cloud NoSQL)
* **External APIs**: 
    * Malaysian Open Data API (Weather)
    * Hugging Face Inference API (Sentiment Analysis)

## 🚀 Getting Started

### Prerequisites
* **JDK 24** or higher installed.
* An active **MongoDB Atlas** cluster.
* A **Hugging Face** API Bearer Token.

### Installation & Setup

1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/your-username/ctrl-c-ctrl-v.git
    cd ctrl-c-ctrl-v/ctrl
    ```

2.  **Configure Environment Variables**:
    Create a `.env` file in the `ctrl/` directory and add your credentials:
    ```text
    MONGODB_URI=your_mongodb_atlas_uri
    BEARER_TOKEN=your_huggingface_api_token
    ```

3.  **Run the Application**:
    Using the Maven Wrapper scripts:
    * **Windows**: `mvnw.cmd javafx:run`
    * **macOS/Linux**: `./mvnw javafx:run`

## 🏗️ Architecture

The project follows the **Model-View-Controller (MVC)** architectural pattern to ensure clean separation of concerns:
* **Model**: Manages data logic and MongoDB/File system interactions.
* **View**: Handles the visual interface via FXML and CSS styling.
* **Controller**: Intercepts user events and orchestrates data flow between the APIs and the UI.

## 👥 Contributors (Group: Ctrl+C Ctrl+V)

* **LIM HONG ZHANG** (Matric: 25006100)
* **TAN CHEE KEAT** (Matric: 25006123)
* **LEE MING DAO** (Matric: 25006825)
* **JITESH A/L MOGANA RAJA** (Matric: 25006745)
* **TEH XU ZHE** (Matric: 25006355)

## 🔮 Future Improvements

* **Dynamic Geolocation**: Transition from static city data to IP-based location tracking.
* **Voice-to-Text**: Integration of Speech-to-Text engines for hands-free journaling.
* **Mobile Companion**: Developing a cross-platform mobile app that syncs with the desktop database.
* **End-to-End Encryption**: Implementing client-side AES-256 encryption for all journal text.