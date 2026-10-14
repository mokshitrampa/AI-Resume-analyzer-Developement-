import React, { useState } from 'react';
import DashboardLayout from '../../layouts/DashboardLayout';
import { authService } from '../../services/authService';
import { useAuth } from '../../context/AuthContext';
import {
  Settings,
  Lock,
  Shield,
  Key,
  CheckCircle2,
  AlertCircle,
  Save,
  Moon,
  Sun,
  Trash2
} from 'lucide-react';

const SettingsPage = () => {
  const { user, logout } = useAuth();

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  const handleChangePassword = async (e) => {
    e.preventDefault();
    setSuccess('');
    setError('');

    if (newPassword !== confirmPassword) {
      setError('New passwords do not match.');
      return;
    }

    if (newPassword.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }

    setLoading(true);

    try {
      await authService.changePassword(currentPassword, newPassword);
      setSuccess('Password updated successfully!');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Failed to change password. Please verify current password.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <DashboardLayout
      title="Account Settings"
      subtitle="Manage your security credentials, privacy preferences, and application configurations."
    >
      <div className="max-w-3xl mx-auto space-y-6">
        {success && (
          <div className="p-4 rounded-2xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 text-emerald-800 dark:text-emerald-300 text-sm flex items-center gap-2">
            <CheckCircle2 className="w-5 h-5 flex-shrink-0 text-emerald-600" />
            <span>{success}</span>
          </div>
        )}

        {error && (
          <div className="p-4 rounded-2xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-800 dark:text-rose-300 text-sm flex items-center gap-2">
            <AlertCircle className="w-5 h-5 flex-shrink-0 text-rose-600" />
            <span>{error}</span>
          </div>
        )}

        {/* Change Password Card */}
        <form onSubmit={handleChangePassword} className="bg-white dark:bg-slate-900 rounded-3xl p-6 sm:p-8 border border-slate-200 dark:border-slate-800 shadow-sm space-y-5">
          <h3 className="font-bold text-base text-slate-900 dark:text-white flex items-center gap-2">
            <Key className="w-5 h-5 text-blue-600" />
            Change Password
          </h3>
          <p className="text-xs text-slate-500">
            Ensure your account is using a secure, unique password.
          </p>

          <div>
            <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
              Current Password
            </label>
            <input
              type="password"
              required
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                New Password
              </label>
              <input
                type="password"
                required
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-1.5">
                Confirm New Password
              </label>
              <input
                type="password"
                required
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div className="pt-2">
            <button
              type="submit"
              disabled={loading}
              className="inline-flex items-center gap-2 px-6 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm shadow-sm disabled:opacity-60 transition-colors cursor-pointer"
            >
              <Save className="w-4 h-4" />
              {loading ? 'Updating...' : 'Update Password'}
            </button>
          </div>
        </form>

        {/* Privacy & Data Ownership Section */}
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 sm:p-8 border border-slate-200 dark:border-slate-800 shadow-sm space-y-4">
          <h3 className="font-bold text-base text-slate-900 dark:text-white flex items-center gap-2">
            <Shield className="w-5 h-5 text-indigo-600" />
            Privacy &amp; Data Ownership
          </h3>
          <p className="text-xs text-slate-500 leading-relaxed">
            Your uploaded documents are securely stored and processed exclusively for calculating your match scores and quality benchmarks.
            Recruiters cannot view your private resume documents unless you explicitly submit an application to their listed vacancy.
          </p>

          <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-950/60 border border-slate-200/80 dark:border-slate-800 flex items-center justify-between text-xs">
            <div>
              <span className="font-semibold text-slate-800 dark:text-slate-200 block">
                Logged in Account:
              </span>
              <span className="text-slate-500">{user?.email} ({user?.role})</span>
            </div>
            <button
              type="button"
              onClick={logout}
              className="px-4 py-2 rounded-xl text-rose-600 border border-rose-200 dark:border-rose-900/40 hover:bg-rose-50 text-xs font-semibold cursor-pointer"
            >
              Sign Out
            </button>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default SettingsPage;
