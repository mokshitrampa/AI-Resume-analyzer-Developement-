import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import DashboardLayout from '../../layouts/DashboardLayout';
import ScoreRing from '../../components/ScoreRing';
import LoadingSpinner from '../../components/LoadingSpinner';
import { resumeService } from '../../services/resumeService';
import {
  Award,
  CheckCircle2,
  AlertTriangle,
  HelpCircle,
  Sparkles,
  ArrowRight,
  Copy,
  Printer,
  FileText,
  ChevronRight,
  Layers,
  Bot,
  Cpu
} from 'lucide-react';

const ResumeAnalyzerPage = () => {
  const [searchParams] = useSearchParams();
  const urlResumeId = searchParams.get('id');

  const [resumes, setResumes] = useState([]);
  const [selectedResumeId, setSelectedResumeId] = useState(urlResumeId || '');
  const [analysis, setAnalysis] = useState(null);
  const [improvements, setImprovements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [analyzing, setAnalyzing] = useState(false);
  const [copiedIndex, setCopiedIndex] = useState(null);

  useEffect(() => {
    loadResumes();
  }, []);

  useEffect(() => {
    if (selectedResumeId) {
      loadAnalysisData(selectedResumeId);
    }
  }, [selectedResumeId]);

  const loadResumes = async () => {
    try {
      setLoading(true);
      const list = await resumeService.getAllResumes();
      setResumes(list);
      if (list.length > 0 && !selectedResumeId) {
        setSelectedResumeId(list[0].id);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const loadAnalysisData = async (resumeId) => {
    try {
      setAnalyzing(true);
      // Fetch or trigger analysis
      const analysisData = await resumeService.analyzeResume(resumeId);
      setAnalysis(analysisData);

      const impData = await resumeService.getAiImprovementSuggestions(resumeId);
      setImprovements(impData || []);
    } catch (err) {
      console.error(err);
    } finally {
      setAnalyzing(false);
    }
  };

  const handleCopy = (text, index) => {
    navigator.clipboard.writeText(text);
    setCopiedIndex(index);
    setTimeout(() => setCopiedIndex(null), 2000);
  };

  const handlePrint = () => {
    window.print();
  };

  if (loading) {
    return (
      <DashboardLayout title="Resume Quality Analyzer">
        <LoadingSpinner text="Loading resume profile..." size="lg" />
      </DashboardLayout>
    );
  }

  if (resumes.length === 0) {
    return (
      <DashboardLayout title="Resume Quality Analyzer">
        <div className="bg-white dark:bg-slate-900 rounded-3xl p-12 text-center border border-slate-200 dark:border-slate-800 shadow-sm max-w-xl mx-auto">
          <FileText className="w-12 h-12 text-slate-300 dark:text-slate-700 mx-auto mb-4" />
          <h3 className="text-lg font-bold text-slate-900 dark:text-white">No Resumes Uploaded Yet</h3>
          <p className="text-sm text-slate-500 mt-2 mb-6">
            Upload your PDF or DOCX resume to calculate your deterministic quality score and receive AI phrasing suggestions.
          </p>
          <Link
            to="/student/upload"
            className="inline-flex items-center gap-2 px-6 py-2.5 rounded-xl bg-blue-600 text-white font-semibold text-sm hover:bg-blue-700 transition-colors"
          >
            Upload Resume Now
          </Link>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout
      title="Resume Quality Analyzer"
      subtitle="Evaluated against standard academic and hiring rubrics for completeness, skills, and readability."
      action={
        <div className="flex items-center gap-3">
          <select
            value={selectedResumeId}
            onChange={(e) => setSelectedResumeId(e.target.value)}
            className="px-3.5 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-200 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            {resumes.map((r) => (
              <option key={r.id} value={r.id}>
                {r.originalFilename} (v{r.version})
              </option>
            ))}
          </select>
          <button
            type="button"
            onClick={handlePrint}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 hover:bg-slate-50 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 text-sm font-medium transition-colors cursor-pointer"
          >
            <Printer className="w-4 h-4" />
            Print Report
          </button>
        </div>
      }
    >
      {analyzing ? (
        <LoadingSpinner text="Computing deterministic quality score and extracting insights..." size="lg" />
      ) : analysis ? (
        <div className="space-y-8">
          {/* Top Score Banner & Category Breakdown */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Score Ring Card */}
            <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col items-center justify-center text-center">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
                Overall Quality Score
              </span>
              <ScoreRing score={analysis.overallScore} size={160} strokeWidth={12} label="Quality Benchmark" sublabel="Estimated" />
              <p className="text-xs text-slate-500 mt-4 max-w-xs">
                Note: This score is calculated via deterministic heuristic algorithms and is not an official ATS guarantee.
              </p>
              <div className="mt-6 pt-4 border-t border-slate-100 dark:border-slate-800 w-full flex justify-between text-xs text-slate-500">
                <span>Evaluated On:</span>
                <span className="font-semibold text-slate-700 dark:text-slate-300">
                  {new Date(analysis.analyzedAt).toLocaleDateString()}
                </span>
              </div>
            </div>

            {/* Rubric Category Progress Bars */}
            <div className="lg:col-span-2 bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm">
              <h3 className="font-bold text-base text-slate-900 dark:text-white mb-1">
                Category-Wise Performance Rubric
              </h3>
              <p className="text-xs text-slate-500 mb-6">
                Breakdown of how your document performs across essential technical hiring criteria.
              </p>

              <div className="space-y-4">
                <div>
                  <div className="flex justify-between text-xs font-semibold mb-1">
                    <span className="text-slate-700 dark:text-slate-300">Resume Completeness</span>
                    <span className="text-blue-600 dark:text-blue-400">{analysis.completenessScore}%</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                    <div className="h-full bg-blue-600 rounded-full" style={{ width: `${analysis.completenessScore}%` }} />
                  </div>
                </div>

                <div>
                  <div className="flex justify-between text-xs font-semibold mb-1">
                    <span className="text-slate-700 dark:text-slate-300">Technical Skill Coverage</span>
                    <span className="text-indigo-600 dark:text-indigo-400">{analysis.skillCoverageScore}%</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                    <div className="h-full bg-indigo-600 rounded-full" style={{ width: `${analysis.skillCoverageScore}%` }} />
                  </div>
                </div>

                <div>
                  <div className="flex justify-between text-xs font-semibold mb-1">
                    <span className="text-slate-700 dark:text-slate-300">Project Descriptions &amp; Scope</span>
                    <span className="text-purple-600 dark:text-purple-400">{analysis.projectScore}%</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                    <div className="h-full bg-purple-600 rounded-full" style={{ width: `${analysis.projectScore}%` }} />
                  </div>
                </div>

                <div>
                  <div className="flex justify-between text-xs font-semibold mb-1">
                    <span className="text-slate-700 dark:text-slate-300">Education Details</span>
                    <span className="text-emerald-600 dark:text-emerald-400">{analysis.educationScore}%</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                    <div className="h-full bg-emerald-600 rounded-full" style={{ width: `${analysis.educationScore}%` }} />
                  </div>
                </div>

                <div>
                  <div className="flex justify-between text-xs font-semibold mb-1">
                    <span className="text-slate-700 dark:text-slate-300">Formatting &amp; Readability</span>
                    <span className="text-amber-600 dark:text-amber-400">{analysis.formattingScore}%</span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                    <div className="h-full bg-amber-600 rounded-full" style={{ width: `${analysis.formattingScore}%` }} />
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Strengths, Weaknesses, and Missing Info */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Strengths */}
            <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm">
              <h4 className="font-bold text-sm text-emerald-700 dark:text-emerald-400 flex items-center gap-2 mb-4">
                <CheckCircle2 className="w-4 h-4" />
                Detected Strengths ({analysis.strengths?.length || 0})
              </h4>
              <ul className="space-y-2 text-xs text-slate-600 dark:text-slate-300">
                {analysis.strengths?.map((s, idx) => (
                  <li key={idx} className="flex items-start gap-2">
                    <span className="text-emerald-500 font-bold">•</span>
                    <span>{s}</span>
                  </li>
                ))}
              </ul>
            </div>

            {/* Weaknesses */}
            <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm">
              <h4 className="font-bold text-sm text-amber-700 dark:text-amber-400 flex items-center gap-2 mb-4">
                <AlertTriangle className="w-4 h-4" />
                Areas to Strengthen ({analysis.weaknesses?.length || 0})
              </h4>
              <ul className="space-y-2 text-xs text-slate-600 dark:text-slate-300">
                {analysis.weaknesses?.map((w, idx) => (
                  <li key={idx} className="flex items-start gap-2">
                    <span className="text-amber-500 font-bold">•</span>
                    <span>{w}</span>
                  </li>
                ))}
              </ul>
            </div>

            {/* Missing Info */}
            <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm">
              <h4 className="font-bold text-sm text-rose-700 dark:text-rose-400 flex items-center gap-2 mb-4">
                <HelpCircle className="w-4 h-4" />
                Missing Information ({analysis.missingInfo?.length || 0})
              </h4>
              {analysis.missingInfo?.length > 0 ? (
                <ul className="space-y-2 text-xs text-slate-600 dark:text-slate-300">
                  {analysis.missingInfo.map((m, idx) => (
                    <li key={idx} className="flex items-start gap-2">
                      <span className="text-rose-500 font-bold">•</span>
                      <span>{m}</span>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="text-xs text-slate-500">All standard resume sections are present and accounted for!</p>
              )}
            </div>
          </div>

          {/* AI Phrasing & Improvement Assistant */}
          <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200 dark:border-slate-800 shadow-sm">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
              <div>
                <h3 className="font-bold text-lg text-slate-900 dark:text-white flex items-center gap-2">
                  <Sparkles className="w-5 h-5 text-indigo-600" />
                  AI Phrasing &amp; Action Verb Assistant
                </h3>
                <p className="text-xs text-slate-500 mt-1">
                  Side-by-side comparison transforming passive phrasing into high-impact engineering accomplishments.
                </p>
              </div>
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-100 dark:bg-slate-800 text-xs font-medium text-slate-600 dark:text-slate-300">
                {improvements[0]?.aiGenerated ? (
                  <>
                    <Bot className="w-3.5 h-3.5 text-purple-600" />
                    <span>Real AI Model Feedback</span>
                  </>
                ) : (
                  <>
                    <Cpu className="w-3.5 h-3.5 text-blue-600" />
                    <span>Deterministic Rule-Based Engine</span>
                  </>
                )}
              </div>
            </div>

            <div className="space-y-4">
              {improvements.map((item, idx) => (
                <div
                  key={idx}
                  className="p-5 rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-950/40 space-y-3"
                >
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {/* Original bullet */}
                    <div className="p-3.5 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800">
                      <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block mb-1">
                        Original Text
                      </span>
                      <p className="text-xs text-slate-700 dark:text-slate-300 italic">
                        "{item.originalText}"
                      </p>
                    </div>

                    {/* Improved suggestion */}
                    <div className="p-3.5 rounded-xl bg-blue-50/60 dark:bg-blue-950/30 border border-blue-200/60 dark:border-blue-900/40 relative group">
                      <div className="flex items-center justify-between mb-1">
                        <span className="text-[10px] font-bold uppercase tracking-wider text-blue-700 dark:text-blue-300 flex items-center gap-1">
                          <Sparkles className="w-3 h-3 text-blue-600" />
                          Recommended Upgrade ({item.category})
                        </span>
                        <button
                          type="button"
                          onClick={() => handleCopy(item.suggestedText, idx)}
                          className="text-xs text-blue-600 dark:text-blue-400 hover:text-blue-800 flex items-center gap-1 cursor-pointer font-medium"
                          title="Copy to clipboard"
                        >
                          <Copy className="w-3 h-3" />
                          {copiedIndex === idx ? 'Copied!' : 'Copy'}
                        </button>
                      </div>
                      <p className="text-xs text-slate-800 dark:text-slate-200 font-medium">
                        "{item.suggestedText}"
                      </p>
                    </div>
                  </div>

                  <p className="text-[11px] text-slate-500 pl-1">
                    <span className="font-semibold text-slate-700 dark:text-slate-300">Rationale:</span> {item.reason}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </div>
      ) : null}
    </DashboardLayout>
  );
};

export default ResumeAnalyzerPage;
