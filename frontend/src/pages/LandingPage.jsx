import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import ScoreRing from '../components/ScoreRing';
import SkillBadge from '../components/SkillBadge';
import {
  FileText,
  Sparkles,
  ArrowRight,
  CheckCircle2,
  TrendingUp,
  Briefcase,
  ShieldAlert,
  Search,
  Code2,
  Cpu,
  Layers,
  Upload,
  BarChart3,
  Award,
  ChevronRight,
  Database
} from 'lucide-react';

const LandingPage = () => {
  const [selectedDemoRole, setSelectedDemoRole] = useState('java');

  const demoRoles = {
    java: {
      title: 'Senior Java Backend Engineer',
      company: 'Enterprise Cloud Systems',
      reqSkills: ['Java 21', 'Spring Boot 3', 'REST API', 'MySQL', 'Docker', 'Git'],
      userSkills: ['Java 21', 'Spring Boot 3', 'REST API', 'MySQL', 'Git', 'Python'],
      matchScore: 84,
      breakdown: {
        requiredSkills: 83,
        preferredSkills: 75,
        semanticSimilarity: 88,
        experience: 85,
        education: 90,
      },
      missingSkills: ['Docker', 'Kubernetes'],
      matchedSkills: ['Java 21', 'Spring Boot 3', 'REST API', 'MySQL', 'Git'],
    },
    fullstack: {
      title: 'Full Stack Web Developer',
      company: 'FinTech Innovations',
      reqSkills: ['React', 'JavaScript', 'Tailwind CSS', 'Node.js', 'PostgreSQL'],
      userSkills: ['React', 'JavaScript', 'HTML5', 'CSS3', 'Tailwind CSS', 'Java'],
      matchScore: 78,
      breakdown: {
        requiredSkills: 80,
        preferredSkills: 70,
        semanticSimilarity: 82,
        experience: 75,
        education: 85,
      },
      missingSkills: ['Node.js', 'PostgreSQL'],
      matchedSkills: ['React', 'JavaScript', 'Tailwind CSS'],
    },
  };

  const activeDemo = demoRoles[selectedDemoRole];

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col transition-colors">
      <Navbar />

      {/* Hero Section */}
      <section className="relative overflow-hidden pt-12 pb-20 lg:pt-20 lg:pb-28">
        <div className="absolute inset-0 -z-10 bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(120,119,198,0.18),rgba(255,255,255,0))] dark:bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(59,130,246,0.15),rgba(0,0,0,0))]" />
        
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-blue-50 dark:bg-blue-950/60 border border-blue-200 dark:border-blue-800/60 text-blue-700 dark:text-blue-300 text-xs font-semibold uppercase tracking-wider mb-6 shadow-sm">
            <Sparkles className="w-4 h-4 text-blue-600 animate-pulse" />
            Java 21 & Spring Boot 3 Powered Recruitment Intelligence
          </div>

          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-slate-900 dark:text-white max-w-4xl mx-auto leading-tight">
            Analyze Your Resume.{' '}
            <span className="bg-gradient-to-r from-blue-600 via-indigo-600 to-purple-600 bg-clip-text text-transparent">
              Discover Your Potential.
            </span>{' '}
            Find Your Perfect Match.
          </h1>

          <p className="mt-6 text-lg sm:text-xl text-slate-600 dark:text-slate-300 max-w-2xl mx-auto leading-relaxed">
            Upload your resume in PDF or DOCX format. Our deterministic NLP engine extracts your skills, benchmarks quality, calculates explainable match scores, and recommends personalized learning roadmaps.
          </p>

          <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link
              to="/student/upload"
              className="w-full sm:w-auto inline-flex items-center justify-center gap-2.5 px-8 py-3.5 rounded-2xl bg-gradient-to-r from-blue-600 via-indigo-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white font-semibold text-base shadow-lg shadow-indigo-500/25 hover:shadow-xl hover:shadow-indigo-500/35 transition-all duration-200 hover:-translate-y-0.5"
            >
              <Upload className="w-5 h-5" />
              Upload Resume Now
              <ArrowRight className="w-4 h-4" />
            </Link>
            <a
              href="#preview"
              className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-2xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200 font-semibold text-base shadow-sm transition-all"
            >
              Explore Live Interactive Demo
            </a>
          </div>

          {/* Quick Metrics Banner */}
          <div className="mt-16 grid grid-cols-2 sm:grid-cols-4 gap-4 max-w-4xl mx-auto text-left">
            <div className="bg-white/70 dark:bg-slate-900/70 backdrop-blur-md p-5 rounded-2xl border border-slate-200/80 dark:border-slate-800 shadow-sm">
              <span className="text-3xl font-extrabold text-blue-600 dark:text-blue-400">100%</span>
              <p className="text-xs font-semibold text-slate-600 dark:text-slate-300 uppercase tracking-wider mt-1">Deterministic Scoring</p>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">Fully explainable rubric</p>
            </div>
            <div className="bg-white/70 dark:bg-slate-900/70 backdrop-blur-md p-5 rounded-2xl border border-slate-200/80 dark:border-slate-800 shadow-sm">
              <span className="text-3xl font-extrabold text-indigo-600 dark:text-indigo-400">TF-IDF</span>
              <p className="text-xs font-semibold text-slate-600 dark:text-slate-300 uppercase tracking-wider mt-1">Cosine Similarity</p>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">Pure Java vectorization</p>
            </div>
            <div className="bg-white/70 dark:bg-slate-900/70 backdrop-blur-md p-5 rounded-2xl border border-slate-200/80 dark:border-slate-800 shadow-sm">
              <span className="text-3xl font-extrabold text-purple-600 dark:text-purple-400">PDF &amp; DOCX</span>
              <p className="text-xs font-semibold text-slate-600 dark:text-slate-300 uppercase tracking-wider mt-1">Apache PDFBox &amp; POI</p>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">Automated parsing</p>
            </div>
            <div className="bg-white/70 dark:bg-slate-900/70 backdrop-blur-md p-5 rounded-2xl border border-slate-200/80 dark:border-slate-800 shadow-sm">
              <span className="text-3xl font-extrabold text-emerald-600 dark:text-emerald-400">5-Phase</span>
              <p className="text-xs font-semibold text-slate-600 dark:text-slate-300 uppercase tracking-wider mt-1">Skill Gap Roadmap</p>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">Custom project plans</p>
            </div>
          </div>
        </div>
      </section>

      {/* Interactive Sample Analysis Preview */}
      <section id="preview" className="py-16 bg-white dark:bg-slate-900 border-y border-slate-200 dark:border-slate-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-12">
            <span className="text-xs font-bold uppercase tracking-wider text-blue-600 dark:text-blue-400">
              Interactive Preview
            </span>
            <h2 className="text-3xl font-bold text-slate-900 dark:text-white mt-2">
              Transparent, Explainable Match Breakdown
            </h2>
            <p className="text-slate-600 dark:text-slate-400 mt-3 text-sm sm:text-base">
              No black-box mysterious numbers. Inspect how candidate skills compare against actual job requirements with weighted math.
            </p>

            <div className="mt-6 inline-flex rounded-xl p-1 bg-slate-100 dark:bg-slate-800 border border-slate-200 dark:border-slate-700">
              <button
                type="button"
                onClick={() => setSelectedDemoRole('java')}
                className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
                  selectedDemoRole === 'java'
                    ? 'bg-white dark:bg-slate-900 text-blue-600 dark:text-blue-400 shadow-sm'
                    : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
                }`}
              >
                Java Backend Role
              </button>
              <button
                type="button"
                onClick={() => setSelectedDemoRole('fullstack')}
                className={`px-4 py-2 rounded-lg text-sm font-semibold transition-all ${
                  selectedDemoRole === 'fullstack'
                    ? 'bg-white dark:bg-slate-900 text-blue-600 dark:text-blue-400 shadow-sm'
                    : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
                }`}
              >
                Full Stack Role
              </button>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 items-start">
            {/* Score Ring & Overview Card */}
            <div className="bg-slate-50 dark:bg-slate-950 p-6 rounded-3xl border border-slate-200 dark:border-slate-800 flex flex-col items-center text-center shadow-sm">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-500 mb-4">Job Match Score</span>
              <ScoreRing score={activeDemo.matchScore} size={150} strokeWidth={12} label="Compatibility" sublabel="Calculated" />
              
              <div className="w-full mt-6 pt-6 border-t border-slate-200 dark:border-slate-800 space-y-3">
                <div className="flex justify-between text-sm">
                  <span className="text-slate-600 dark:text-slate-400 font-medium">Target Role:</span>
                  <span className="font-semibold text-slate-900 dark:text-white truncate max-w-[180px]">{activeDemo.title}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-slate-600 dark:text-slate-400 font-medium">Company:</span>
                  <span className="font-semibold text-slate-900 dark:text-white">{activeDemo.company}</span>
                </div>
              </div>

              <Link
                to="/register"
                className="mt-6 w-full py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-medium text-sm transition-colors text-center shadow-sm"
              >
                Analyze Your Own Resume
              </Link>
            </div>

            {/* Formula & Breakdown Card */}
            <div className="bg-slate-50 dark:bg-slate-950 p-6 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm">
              <div className="flex items-center gap-2 mb-4">
                <Cpu className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />
                <h3 className="font-bold text-slate-900 dark:text-white text-base">Weighted Formula Breakdown</h3>
              </div>
              <p className="text-xs text-slate-500 mb-4">
                Normalized formula: 40% Req Skills + 20% Pref Skills + 20% TF-IDF Cosine Similarity + 10% Experience + 10% Education.
              </p>

              <div className="space-y-3.5">
                <div>
                  <div className="flex justify-between text-xs font-semibold mb-1">
                    <span className="text-slate-700 dark:text-slate-300">Required Skills Overlap (40%)</span>
                    <span className="text-blue-600 dark:text-blue-400">{activeDemo.breakdown.requiredSkills}%</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-200 dark:bg-slate-800 overflow-hidden">
                    <div className="h-full bg-blue-600 rounded-full" style={{ width: `${activeDemo.breakdown.requiredSkills}%` }} />
                  </div>
                </div>

                <div>
                  <div className="flex justify-between text-xs font-semibold mb-1">
                    <span className="text-slate-700 dark:text-slate-300">Semantic &amp; Text TF-IDF (20%)</span>
                    <span className="text-indigo-600 dark:text-indigo-400">{activeDemo.breakdown.semanticSimilarity}%</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-200 dark:bg-slate-800 overflow-hidden">
                    <div className="h-full bg-indigo-600 rounded-full" style={{ width: `${activeDemo.breakdown.semanticSimilarity}%` }} />
                  </div>
                </div>

                <div>
                  <div className="flex justify-between text-xs font-semibold mb-1">
                    <span className="text-slate-700 dark:text-slate-300">Preferred Skills (20%)</span>
                    <span className="text-purple-600 dark:text-purple-400">{activeDemo.breakdown.preferredSkills}%</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-200 dark:bg-slate-800 overflow-hidden">
                    <div className="h-full bg-purple-600 rounded-full" style={{ width: `${activeDemo.breakdown.preferredSkills}%` }} />
                  </div>
                </div>

                <div>
                  <div className="flex justify-between text-xs font-semibold mb-1">
                    <span className="text-slate-700 dark:text-slate-300">Experience Alignment (10%)</span>
                    <span className="text-emerald-600 dark:text-emerald-400">{activeDemo.breakdown.experience}%</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-200 dark:bg-slate-800 overflow-hidden">
                    <div className="h-full bg-emerald-600 rounded-full" style={{ width: `${activeDemo.breakdown.experience}%` }} />
                  </div>
                </div>

                <div>
                  <div className="flex justify-between text-xs font-semibold mb-1">
                    <span className="text-slate-700 dark:text-slate-300">Education &amp; Qualification (10%)</span>
                    <span className="text-amber-600 dark:text-amber-400">{activeDemo.breakdown.education}%</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-200 dark:bg-slate-800 overflow-hidden">
                    <div className="h-full bg-amber-600 rounded-full" style={{ width: `${activeDemo.breakdown.education}%` }} />
                  </div>
                </div>
              </div>
            </div>

            {/* Detected & Missing Skills Card */}
            <div className="bg-slate-50 dark:bg-slate-950 p-6 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm space-y-6">
              <div>
                <h4 className="text-xs font-bold uppercase tracking-wider text-emerald-600 dark:text-emerald-400 flex items-center gap-1.5 mb-3">
                  <CheckCircle2 className="w-4 h-4" />
                  Matched Skills ({activeDemo.matchedSkills.length})
                </h4>
                <div className="flex flex-wrap gap-2">
                  {activeDemo.matchedSkills.map((s) => (
                    <SkillBadge key={s} name={s} variant="matched" size="sm" />
                  ))}
                </div>
              </div>

              <div className="pt-4 border-t border-slate-200 dark:border-slate-800">
                <h4 className="text-xs font-bold uppercase tracking-wider text-rose-600 dark:text-rose-400 flex items-center gap-1.5 mb-3">
                  <ShieldAlert className="w-4 h-4" />
                  Identified Skill Gaps ({activeDemo.missingSkills.length})
                </h4>
                <div className="flex flex-wrap gap-2">
                  {activeDemo.missingSkills.map((s) => (
                    <SkillBadge key={s} name={s} variant="missing" size="sm" />
                  ))}
                </div>
                <p className="text-xs text-slate-500 mt-3">
                  Our Skill Gap engine will generate targeted tutorials, portfolio ideas, and milestones to bridge these gaps.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section id="how-it-works" className="py-20 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <span className="text-xs font-bold uppercase tracking-wider text-blue-600 dark:text-blue-400">
            Workflow Architecture
          </span>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white mt-2">
            How The Analysis Pipeline Works
          </h2>
          <p className="text-slate-600 dark:text-slate-400 mt-3 text-base">
            From binary document parsing to weighted mathematical matching, see the end-to-end workflow executed right inside our Spring Boot backend.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div className="relative bg-white dark:bg-slate-900 p-6 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col">
            <div className="w-12 h-12 rounded-xl bg-blue-50 dark:bg-blue-950/50 text-blue-600 dark:text-blue-400 flex items-center justify-center font-bold text-lg mb-4">
              01
            </div>
            <h3 className="font-bold text-base text-slate-900 dark:text-white">Document Ingestion</h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-2 leading-relaxed">
              Upload PDF or DOCX up to 5MB. Apache PDFBox and POI extract plain structured text while validating MIME signatures and sanitizing filenames.
            </p>
          </div>

          <div className="relative bg-white dark:bg-slate-900 p-6 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col">
            <div className="w-12 h-12 rounded-xl bg-indigo-50 dark:bg-indigo-950/50 text-indigo-600 dark:text-indigo-400 flex items-center justify-center font-bold text-lg mb-4">
              02
            </div>
            <h3 className="font-bold text-base text-slate-900 dark:text-white">NLP Skill Extraction</h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-2 leading-relaxed">
              Normalized regex and alias dictionary map variations (e.g. "JS" → JavaScript, "k8s" → Kubernetes). User can review and confirm detected data.
            </p>
          </div>

          <div className="relative bg-white dark:bg-slate-900 p-6 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col">
            <div className="w-12 h-12 rounded-xl bg-purple-50 dark:bg-purple-950/50 text-purple-600 dark:text-purple-400 flex items-center justify-center font-bold text-lg mb-4">
              03
            </div>
            <h3 className="font-bold text-base text-slate-900 dark:text-white">TF-IDF Vector Matching</h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-2 leading-relaxed">
              Resumes and job descriptions are tokenized, stopwords removed, and vector transformed to calculate pure Java cosine similarity.
            </p>
          </div>

          <div className="relative bg-white dark:bg-slate-900 p-6 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col">
            <div className="w-12 h-12 rounded-xl bg-emerald-50 dark:bg-emerald-950/50 text-emerald-600 dark:text-emerald-400 flex items-center justify-center font-bold text-lg mb-4">
              04
            </div>
            <h3 className="font-bold text-base text-slate-900 dark:text-white">Explainable Roadmap</h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-2 leading-relaxed">
              Dynamic missing skill identification outputs a 5-phase career learning roadmap and concrete portfolio project recommendations.
            </p>
          </div>
        </div>
      </section>

      {/* Features Grid */}
      <section id="features" className="py-20 bg-slate-100/60 dark:bg-slate-900/40 border-t border-slate-200 dark:border-slate-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <span className="text-xs font-bold uppercase tracking-wider text-blue-600 dark:text-blue-400">
              Core Capabilities
            </span>
            <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white mt-2">
              Everything Needed for Modern Career Matching
            </h2>
            <p className="text-slate-600 dark:text-slate-400 mt-3 text-base">
              Engineered with clean separation of concerns, secure role-based permissions, and transparent scoring rubrics.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-white dark:bg-slate-900 p-8 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-md transition-shadow">
              <div className="w-12 h-12 rounded-2xl bg-blue-50 dark:bg-blue-950/50 text-blue-600 dark:text-blue-400 flex items-center justify-center mb-6">
                <Award className="w-6 h-6" />
              </div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-2">Resume Quality Scorer</h3>
              <p className="text-sm text-slate-600 dark:text-slate-400 leading-relaxed">
                Evaluates resume completeness, formatting, action verbs, project descriptions, and technical coverage on an objective 0-100 rubric.
              </p>
            </div>

            <div className="bg-white dark:bg-slate-900 p-8 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-md transition-shadow">
              <div className="w-12 h-12 rounded-2xl bg-indigo-50 dark:bg-indigo-950/50 text-indigo-600 dark:text-indigo-400 flex items-center justify-center mb-6">
                <TrendingUp className="w-6 h-6" />
              </div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-2">Skill Gap Roadmap</h3>
              <p className="text-sm text-slate-600 dark:text-slate-400 leading-relaxed">
                Highlights exactly what technologies you are missing for specific vacancies and provides actionable learning milestones to master them.
              </p>
            </div>

            <div className="bg-white dark:bg-slate-900 p-8 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm hover:shadow-md transition-shadow">
              <div className="w-12 h-12 rounded-2xl bg-purple-50 dark:bg-purple-950/50 text-purple-600 dark:text-purple-400 flex items-center justify-center mb-6">
                <Briefcase className="w-6 h-6" />
              </div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-2">Recruiter &amp; Admin Suite</h3>
              <p className="text-sm text-slate-600 dark:text-slate-400 leading-relaxed">
                Recruiters publish jobs and view applicant match scores; Administrators moderate postings, manage the skill dictionary, and audit system metrics.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Call to action */}
      <section className="py-20 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <div className="bg-gradient-to-r from-blue-600 via-indigo-600 to-purple-600 rounded-3xl p-8 sm:p-14 text-white shadow-2xl">
          <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
            Ready to Optimize Your Resume for Top Engineering Roles?
          </h2>
          <p className="mt-4 text-blue-100 max-w-xl mx-auto text-base">
            Create your account today, upload your resume, and see how you match against real Java, Python, Cloud, and Full Stack positions.
          </p>
          <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link
              to="/register"
              className="w-full sm:w-auto px-8 py-3.5 rounded-2xl bg-white text-slate-900 font-bold hover:bg-blue-50 transition-colors shadow-lg"
            >
              Register as Student
            </Link>
            <Link
              to="/login"
              className="w-full sm:w-auto px-8 py-3.5 rounded-2xl bg-blue-700/60 hover:bg-blue-700 text-white font-bold border border-white/20 transition-colors"
            >
              Sign In to Account
            </Link>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
};

export default LandingPage;
