import React from 'react';
import { Sparkles, Heart, GraduationCap, ShieldCheck } from 'lucide-react';

const Footer = () => {
  return (
    <footer className="mt-auto bg-slate-900 text-slate-400 border-t border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Brand Info */}
          <div className="md:col-span-2 space-y-4">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-blue-600 to-indigo-600 flex items-center justify-center text-white">
                <Sparkles className="w-4 h-4" />
              </div>
              <span className="font-bold text-lg text-white">ResumeAI & Job Matcher</span>
            </div>
            <p className="text-sm text-slate-400 max-w-md">
              A university full-stack engineering project demonstrating intelligent resume information extraction,
              TF-IDF & cosine similarity matching, deterministic quality scoring, and AI-assisted career roadmaps.
            </p>
            <div className="flex items-center gap-3 pt-2">
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-800 border border-slate-700 text-xs text-slate-300">
                <GraduationCap className="w-3.5 h-3.5 text-blue-400" />
                Java Lab Project
              </span>
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-800 border border-slate-700 text-xs text-slate-300">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
                Deterministic Scoring
              </span>
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-300 mb-4">Platform</h4>
            <ul className="space-y-2 text-sm">
              <li><a href="/#features" className="hover:text-white transition-colors">Features</a></li>
              <li><a href="/#how-it-works" className="hover:text-white transition-colors">Algorithm Details</a></li>
              <li><a href="/#preview" className="hover:text-white transition-colors">Sample Analysis</a></li>
              <li><a href="/login" className="hover:text-white transition-colors">Student Login</a></li>
            </ul>
          </div>

          {/* Tech Stack */}
          <div>
            <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-300 mb-4">Core Stack</h4>
            <ul className="space-y-2 text-sm">
              <li>Java 21 & Spring Boot 3</li>
              <li>React 19 & Vite & Tailwind</li>
              <li>Apache PDFBox & Apache POI</li>
              <li>TF-IDF & Cosine Similarity</li>
              <li>MySQL 8 Database</li>
            </ul>
          </div>
        </div>

        <div className="mt-8 pt-8 border-t border-slate-800 flex flex-col sm:flex-row items-center justify-between text-xs text-slate-500">
          <p>&copy; {new Date().getFullYear()} AI-Powered Resume Analyzer & Job Matcher. Built with Java 21 & Spring Boot.</p>
          <p className="flex items-center gap-1 mt-2 sm:mt-0">
            Engineered with <Heart className="w-3.5 h-3.5 text-rose-500 fill-rose-500" /> for Academic & Lab Evaluation
          </p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
