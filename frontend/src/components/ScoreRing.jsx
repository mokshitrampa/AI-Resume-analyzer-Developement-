import React from 'react';

const ScoreRing = ({ score = 0, size = 120, strokeWidth = 10, label = 'Match Score', sublabel = '' }) => {
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const clampedScore = Math.max(0, Math.min(100, Math.round(score)));
  const strokeDashoffset = circumference - (clampedScore / 100) * circumference;

  const getColor = (s) => {
    if (s >= 75) return '#10b981'; // emerald-500
    if (s >= 50) return '#f59e0b'; // amber-500
    return '#ef4444'; // red-500
  };

  const color = getColor(clampedScore);

  return (
    <div className="flex flex-col items-center justify-center">
      <div className="relative inline-flex items-center justify-center" style={{ width: size, height: size }}>
        <svg width={size} height={size} className="transform -rotate-90">
          <circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke="#e2e8f0"
            strokeWidth={strokeWidth}
            fill="transparent"
            className="dark:stroke-slate-700"
          />
          <circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke={color}
            strokeWidth={strokeWidth}
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
            strokeLinecap="round"
            fill="transparent"
            className="transition-all duration-1000 ease-out"
          />
        </svg>
        <div className="absolute flex flex-col items-center justify-center text-center">
          <span className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white" style={{ color }}>
            {clampedScore}%
          </span>
          {sublabel && <span className="text-[10px] text-slate-500 dark:text-slate-400 font-medium">{sublabel}</span>}
        </div>
      </div>
      {label && <span className="mt-2 text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-300">{label}</span>}
    </div>
  );
};

export default ScoreRing;
