import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  Upload,
  FileSearch,
  Crosshair,
  TrendingUp,
  Compass,
  BookmarkCheck,
  History,
  User,
  Settings,
  LogOut,
  Briefcase,
  PlusCircle,
  Users,
  Shield,
  BookOpen,
  Sparkles,
  ChevronRight
} from 'lucide-react';

const Sidebar = () => {
  const { user, isStudent, isRecruiter, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const studentLinks = [
    { to: '/student', icon: LayoutDashboard, label: 'Dashboard', end: true },
    { to: '/student/upload', icon: Upload, label: 'Upload Resume' },
    { to: '/student/analyzer', icon: FileSearch, label: 'Resume Analyzer' },
    { to: '/student/matcher', icon: Crosshair, label: 'Job Matcher' },
    { to: '/student/skill-gap', icon: TrendingUp, label: 'Skill Gap Analysis' },
    { to: '/student/recommendations', icon: Compass, label: 'Job Recommendations' },
    { to: '/student/saved-jobs', icon: BookmarkCheck, label: 'Saved Jobs' },
    { to: '/student/history', icon: History, label: 'Analysis History' },
    { to: '/student/profile', icon: User, label: 'My Profile' },
    { to: '/student/settings', icon: Settings, label: 'Settings' },
  ];

  const recruiterLinks = [
    { to: '/recruiter', icon: LayoutDashboard, label: 'Dashboard', end: true },
    { to: '/recruiter/post-job', icon: PlusCircle, label: 'Post New Job' },
    { to: '/recruiter/manage-jobs', icon: Briefcase, label: 'Manage Jobs' },
    { to: '/recruiter/applications', icon: Users, label: 'Candidate Review' },
    { to: '/recruiter/profile', icon: User, label: 'Company Profile' },
    { to: '/recruiter/settings', icon: Settings, label: 'Settings' },
  ];

  const adminLinks = [
    { to: '/admin', icon: LayoutDashboard, label: 'Dashboard', end: true },
    { to: '/admin/users', icon: Users, label: 'Manage Users' },
    { to: '/admin/jobs', icon: Briefcase, label: 'Job Moderation' },
    { to: '/admin/skills', icon: BookOpen, label: 'Skill Dictionary' },
    { to: '/admin/system', icon: Shield, label: 'System Health' },
    { to: '/admin/settings', icon: Settings, label: 'Settings' },
  ];

  let links = studentLinks;
  let roleTitle = 'Student Portal';
  if (isRecruiter) {
    links = recruiterLinks;
    roleTitle = 'Recruiter Portal';
  } else if (isAdmin) {
    links = adminLinks;
    roleTitle = 'Admin Portal';
  }

  return (
    <aside className="w-64 bg-white dark:bg-slate-900 border-r border-slate-200 dark:border-slate-800 flex flex-col min-h-[calc(100vh-4rem)] flex-shrink-0 transition-colors">
      {/* Role Banner */}
      <div className="px-6 py-4 border-b border-slate-100 dark:border-slate-800">
        <span className="text-[11px] font-bold tracking-wider uppercase text-blue-600 dark:text-blue-400">
          {roleTitle}
        </span>
        <h3 className="text-sm font-semibold text-slate-800 dark:text-slate-200 truncate mt-0.5">
          {user?.fullName || user?.email}
        </h3>
      </div>

      {/* Nav links */}
      <div className="flex-1 py-4 px-3 space-y-1 overflow-y-auto">
        {links.map((link) => {
          const Icon = link.icon;
          return (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.end}
              className={({ isActive }) =>
                `flex items-center justify-between px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-blue-600 text-white shadow-sm shadow-blue-500/20'
                    : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-slate-100'
                }`
              }
            >
              {({ isActive }) => (
                <>
                  <div className="flex items-center gap-3">
                    <Icon className={`w-4 h-4 ${isActive ? 'text-white' : 'text-slate-400'}`} />
                    <span>{link.label}</span>
                  </div>
                  {isActive && <ChevronRight className="w-3.5 h-3.5 text-white/70" />}
                </>
              )}
            </NavLink>
          );
        })}
      </div>

      {/* Bottom User/Logout */}
      <div className="p-4 border-t border-slate-100 dark:border-slate-800">
        <button
          type="button"
          onClick={handleLogout}
          className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/30 transition-colors"
        >
          <LogOut className="w-4 h-4" />
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
