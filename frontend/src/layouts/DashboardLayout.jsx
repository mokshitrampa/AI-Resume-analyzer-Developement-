import React, { useState } from 'react';
import Navbar from '../components/Navbar';
import Sidebar from '../components/Sidebar';
import { Menu, X } from 'lucide-react';

const DashboardLayout = ({ children, title = '', subtitle = '', action = null }) => {
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col transition-colors">
      <Navbar />

      <div className="flex-1 flex max-w-7xl w-full mx-auto">
        {/* Desktop Sidebar */}
        <div className="hidden lg:block">
          <Sidebar />
        </div>

        {/* Mobile Sidebar Modal/Drawer */}
        {mobileSidebarOpen && (
          <div className="fixed inset-0 z-50 lg:hidden flex">
            <div
              className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm"
              onClick={() => setMobileSidebarOpen(false)}
            />
            <div className="relative flex-1 flex flex-col max-w-xs w-full bg-white dark:bg-slate-900 shadow-xl z-10">
              <div className="absolute top-0 right-0 -mr-12 pt-4">
                <button
                  type="button"
                  onClick={() => setMobileSidebarOpen(false)}
                  className="p-2 rounded-lg text-white hover:bg-white/10"
                >
                  <X className="w-6 h-6" />
                </button>
              </div>
              <Sidebar />
            </div>
          </div>
        )}

        {/* Main Content Area */}
        <main className="flex-1 p-4 sm:p-6 lg:p-8 overflow-y-auto">
          {/* Top Bar on Mobile to toggle sidebar */}
          <div className="lg:hidden flex items-center justify-between pb-4 mb-4 border-b border-slate-200 dark:border-slate-800">
            <button
              type="button"
              onClick={() => setMobileSidebarOpen(true)}
              className="flex items-center gap-2 px-3 py-1.5 rounded-lg border border-slate-200 dark:border-slate-800 text-xs font-semibold text-slate-700 dark:text-slate-300"
            >
              <Menu className="w-4 h-4" />
              Portal Menu
            </button>
          </div>

          {(title || action) && (
            <div className="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
              <div>
                {title && <h1 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">{title}</h1>}
                {subtitle && <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">{subtitle}</p>}
              </div>
              {action && <div>{action}</div>}
            </div>
          )}

          {children}
        </main>
      </div>
    </div>
  );
};

export default DashboardLayout;
