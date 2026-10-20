package com.resumeanalyzer.util;

import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.model.User;

public class SessionManager {
    private static User currentUser;
    private static Resume activeResume;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static Resume getActiveResume() {
        return activeResume;
    }

    public static void setActiveResume(Resume resume) {
        activeResume = resume;
    }

    public static void clear() {
        currentUser = null;
        activeResume = null;
    }
}
