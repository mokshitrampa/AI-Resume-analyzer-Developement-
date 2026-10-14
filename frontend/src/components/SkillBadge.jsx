import React from 'react';
import { CheckCircle2, XCircle, Sparkles } from 'lucide-react';

const SkillBadge = ({ name, variant = 'neutral', onRemove = null, size = 'md' }) => {
  // variants: 'matched', 'missing', 'neutral', 'preferred'
  const getStyles = () => {
    switch (variant) {
      case 'matched':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800/60';
      case 'missing':
        return 'bg-rose-50 text-rose-700 border-rose-200 dark:bg-rose-950/40 dark:text-rose-300 dark:border-rose-800/60';
      case 'preferred':
        return 'bg-indigo-50 text-indigo-700 border-indigo-200 dark:bg-indigo-950/40 dark:text-indigo-300 dark:border-indigo-800/60';
      case 'neutral':
      default:
        return 'bg-slate-100 text-slate-700 border-slate-200 dark:bg-slate-800 dark:text-slate-200 dark:border-slate-700';
    }
  };

  const sizeClasses = size === 'sm' ? 'text-xs px-2.5 py-0.5' : 'text-sm px-3 py-1';

  return (
    <span
      className={`inline-flex items-center gap-1.5 font-medium rounded-full border transition-colors ${sizeClasses} ${getStyles()}`}
    >
      {variant === 'matched' && <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />}
      {variant === 'missing' && <XCircle className="w-3.5 h-3.5 text-rose-500" />}
      {variant === 'preferred' && <Sparkles className="w-3.5 h-3.5 text-indigo-500" />}
      <span>{name}</span>
      {onRemove && (
        <button
          type="button"
          onClick={onRemove}
          className="ml-1 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 focus:outline-none"
        >
          &times;
        </button>
      )}
    </span>
  );
};

export default SkillBadge;
