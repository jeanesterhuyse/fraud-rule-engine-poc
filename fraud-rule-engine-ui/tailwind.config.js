/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './pages/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      // Capitec Brand Colors (from fraud-tyr design system)
      colors: {
        // Primary brand colors
        'cap-red': {
          DEFAULT: '#e41c23',
          50: '#fef2f2',
          100: '#fee2e2',
          200: '#fecaca',
          300: '#fca5a5',
          400: '#f87171',
          500: '#e41c23',
          600: '#c71920',
          700: '#a8161b',
          800: '#7f1116',
          900: '#5c0d10',
        },
        'cap-blue': {
          DEFAULT: '#2f70ef',
          50: '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#2f70ef',
          600: '#2563eb',
          700: '#1d4ba6',
          800: '#1e3a8a',
          900: '#1e293b',
        },
        'cap-deep-blue': {
          DEFAULT: '#004973',
          50: '#f0f9ff',
          100: '#e0f2fe',
          200: '#bae6fd',
          300: '#7dd3fc',
          400: '#38bdf8',
          500: '#0ea5e9',
          600: '#0284c7',
          700: '#003d5c',
          800: '#004973',
          900: '#003552',
        },
        'cap-cyan': {
          DEFAULT: '#00a1e1',
          50: '#ecfeff',
          100: '#cffafe',
          200: '#a5f3fc',
          300: '#67e8f9',
          400: '#22d3ee',
          500: '#00a1e1',
          600: '#0891b2',
          700: '#0e7490',
          800: '#155e75',
          900: '#164e63',
        },
        'cap-green': {
          DEFAULT: '#10b981',
          600: '#059669',
        },
        'cap-orange': {
          DEFAULT: '#f97316',
          600: '#ea580c',
        },
        'cap-yellow': {
          DEFAULT: '#eab308',
          700: '#a16207',
        },
        // Neutral colors
        'cap-grey': {
          DEFAULT: '#f7f7f7',
          50: '#fafafa',
          100: '#f7f7f7',
          200: '#f4f6f8',
          300: '#e9edf3',
          400: '#e1e1e1',
          500: '#cdd4e0',
          600: '#6c757d',
          700: '#3a3a3a',
          800: '#333333',
          900: '#1a1a1a',
        },
        'cap-dark': '#3a3a3a',
        'cap-white': '#ffffff',
        // Semantic colors
        'cap-background': '#f4f6f8',
        'cap-surface': '#ffffff',
        'cap-border': '#e1e1e1',
        'cap-text': '#3a3a3a',
        'cap-text-muted': '#6c757d',
      },
      // Typography
      fontFamily: {
        sans: ['var(--font-nunito-sans)', 'system-ui', 'Arial', 'Helvetica', 'sans-serif'],
        body: ['var(--font-nunito-sans)', 'system-ui', 'Arial', 'Helvetica', 'sans-serif'],
      },
      fontSize: {
        'xs': ['0.75rem', { lineHeight: '1rem' }],      // 12px
        'sm': ['0.875rem', { lineHeight: '1.25rem' }],  // 14px
        'base': ['1rem', { lineHeight: '1.5rem' }],     // 16px
        'lg': ['1.25rem', { lineHeight: '1.75rem' }],   // 20px
        'xl': ['1.5rem', { lineHeight: '2rem' }],       // 24px
        '2xl': ['1.8rem', { lineHeight: '2.25rem' }],   // 28.8px - page titles
        '3xl': ['2rem', { lineHeight: '2.5rem' }],
        '4xl': ['2.5rem', { lineHeight: '3rem' }],
      },
      // Spacing (extends Tailwind's default spacing scale)
      spacing: {
        '18': '4.5rem',   // 72px
        '22': '5.5rem',   // 88px
      },
      // Border radius
      borderRadius: {
        'sm': '4px',
        'DEFAULT': '6px',
        'md': '8px',
        'lg': '10px',
        'xl': '12px',
        '2xl': '14px',
        'card': '10px',
        'panel': '12px',
        'btn': '6px',
      },
      // Box shadows (Capitec style - light and subtle)
      boxShadow: {
        'sm': '0 4px 12px rgba(0, 0, 0, 0.06)',
        'DEFAULT': '0 6px 12px rgba(0, 0, 0, 0.08)',
        'md': '0 8px 20px rgba(0, 0, 0, 0.06)',
        'lg': '0 12px 24px rgba(0, 0, 0, 0.12)',
        'xl': '0 16px 40px rgba(0, 0, 0, 0.12)',
        '2xl': '0 30px 60px rgba(187, 187, 187, 0.6)',
        'card': '0 8px 20px rgba(0, 0, 0, 0.06)',
        'card-hover': '0 30px 60px rgba(187, 187, 187, 0.6)',
        'panel': '0 2px 8px rgba(0, 0, 0, 0.08)',
        'inner': 'inset 0 2px 4px 0 rgba(0, 0, 0, 0.06)',
      },
      // Transitions
      transitionDuration: {
        '200': '200ms',
      },
      transitionTimingFunction: {
        'ease': 'ease',
      },
      // Animation
      keyframes: {
        'fade-up': {
          '0%': {
            opacity: '0',
            transform: 'translateY(30px)',
          },
          '100%': {
            opacity: '1',
            transform: 'translateY(0)',
          },
        },
        'fade-in': {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
      },
      animation: {
        'fade-up': 'fade-up 0.6s ease forwards',
        'fade-in': 'fade-in 0.3s ease-in-out',
      },
    },
  },
  plugins: [],
}

