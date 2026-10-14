import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';

// Public Pages
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';

// Student Portal Pages
import StudentDashboard from './pages/student/StudentDashboard';
import UploadResumePage from './pages/student/UploadResumePage';
import ResumeReviewPage from './pages/student/ResumeReviewPage';
import ResumeAnalyzerPage from './pages/student/ResumeAnalyzerPage';
import JobMatcherPage from './pages/student/JobMatcherPage';
import SkillGapPage from './pages/student/SkillGapPage';
import JobRecommendationsPage from './pages/student/JobRecommendationsPage';
import SavedJobsPage from './pages/student/SavedJobsPage';
import AnalysisHistoryPage from './pages/student/AnalysisHistoryPage';
import StudentProfilePage from './pages/student/StudentProfilePage';
import SettingsPage from './pages/student/SettingsPage';

// Recruiter Portal Pages
import RecruiterDashboard from './pages/recruiter/RecruiterDashboard';
import PostJobPage from './pages/recruiter/PostJobPage';
import ManageJobsPage from './pages/recruiter/ManageJobsPage';
import CandidateReviewPage from './pages/recruiter/CandidateReviewPage';
import RecruiterProfilePage from './pages/recruiter/RecruiterProfilePage';

// Admin Portal Pages
import AdminDashboard from './pages/admin/AdminDashboard';
import UserManagementPage from './pages/admin/UserManagementPage';
import JobModerationPage from './pages/admin/JobModerationPage';
import SkillDictionaryPage from './pages/admin/SkillDictionaryPage';
import SystemHealthPage from './pages/admin/SystemHealthPage';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public Landing & Authentication */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />

          {/* Student Portal Routes */}
          <Route
            path="/student"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <StudentDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/student/upload"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <UploadResumePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/student/review/:id"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <ResumeReviewPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/student/analyzer"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <ResumeAnalyzerPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/student/matcher"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <JobMatcherPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/student/skill-gap"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <SkillGapPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/student/recommendations"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <JobRecommendationsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/student/saved-jobs"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <SavedJobsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/student/history"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <AnalysisHistoryPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/student/profile"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <StudentProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/student/settings"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <SettingsPage />
              </ProtectedRoute>
            }
          />

          {/* Recruiter Portal Routes */}
          <Route
            path="/recruiter"
            element={
              <ProtectedRoute allowedRoles={['RECRUITER']}>
                <RecruiterDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter/post-job"
            element={
              <ProtectedRoute allowedRoles={['RECRUITER']}>
                <PostJobPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter/manage-jobs"
            element={
              <ProtectedRoute allowedRoles={['RECRUITER']}>
                <ManageJobsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter/applications"
            element={
              <ProtectedRoute allowedRoles={['RECRUITER']}>
                <CandidateReviewPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter/profile"
            element={
              <ProtectedRoute allowedRoles={['RECRUITER']}>
                <RecruiterProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter/settings"
            element={
              <ProtectedRoute allowedRoles={['RECRUITER']}>
                <SettingsPage />
              </ProtectedRoute>
            }
          />

          {/* Admin Portal Routes */}
          <Route
            path="/admin"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AdminDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/users"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <UserManagementPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/jobs"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <JobModerationPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/skills"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <SkillDictionaryPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/system"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <SystemHealthPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/settings"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <SettingsPage />
              </ProtectedRoute>
            }
          />

          {/* Catch-all redirect to Landing */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
