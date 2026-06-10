# Capitec Design System

Complete design system and Tailwind configuration for the Fraud Rule Engine UI.

**Last Updated:** June 9, 2026

---

## Brand Colors

### Primary Colors
```css
cap-red: #e41c23        /* Primary brand red */
cap-blue: #2f70ef       /* Primary brand blue */
cap-deep-blue: #004973  /* Headings, important text */
cap-cyan: #00a1e1       /* Accents */
```

### Neutral Colors
```css
cap-white: #ffffff
cap-background: #f4f6f8  /* Page background (warm cream) */
cap-grey-50: #fafafa
cap-grey-100: #f7f7f7
cap-grey-200: #f4f6f8
cap-grey-400: #e1e1e1
cap-text: #3a3a3a       /* Body text */
cap-text-muted: #6c757d /* Secondary text */
```

### Semantic Colors
```css
cap-green: #10b981      /* Success */
cap-orange: #f97316     /* Warning */
cap-yellow: #eab308     /* Caution */
```

---

## Typography

**Font Family:** Nunito Sans  
**Base Size:** 16px (1rem)

### Text Scales
- `text-xs`: 12px - Small labels, badges
- `text-sm`: 14px - Secondary text
- `text-base`: 16px - Body text
- `text-lg`: 20px - Section headers
- `text-xl`: 24px - Card titles
- `text-2xl`: 28.8px - Page titles
- `text-3xl`: 32px - Large stats
- `text-4xl`: 40px - Hero numbers

### Utility Classes
```css
.cap-page-title {
  @apply text-2xl font-bold text-cap-deep-blue mb-4;
}

.cap-section-title {
  @apply text-lg font-semibold text-cap-deep-blue mb-2;
}
```

---

## Components

### Cards
```css
.card {
  @apply bg-cap-white rounded-card shadow-card p-6 mb-4;
}

.cap-card-info {
  @apply bg-cap-blue-50 border border-cap-blue-200;
}

.cap-card-success {
  @apply bg-cap-red-50 border border-cap-red-200;
}
```

**Hover Effect:**
```css
hover:shadow-card-hover transition-all duration-200
```

### Buttons
```css
.btn {
  @apply px-5 py-2.5 rounded-btn font-semibold 
         transition-colors duration-200 text-sm;
}

.btn-primary {
  @apply bg-cap-deep-blue text-white hover:bg-cap-deep-blue-700 
         shadow-sm hover:shadow-md;
}

.btn-secondary {
  @apply bg-cap-grey-300 text-cap-deep-blue 
         hover:bg-cap-grey-400 border border-cap-grey-400;
}

.btn-outline {
  @apply bg-white border border-cap-deep-blue 
         text-cap-deep-blue hover:bg-cap-deep-blue-50;
}

.btn-error {
  @apply bg-white text-cap-red border border-cap-red 
         hover:bg-cap-red hover:text-white;
}

.btn-success {
  @apply bg-cap-red text-white hover:bg-cap-red-600 
         shadow-sm hover:shadow-md;
}
```

### Badges
```css
.badge {
  @apply inline-block px-2.5 py-1 rounded text-xs font-medium;
}

.badge-success {
  @apply bg-cap-red/10 text-cap-red border border-cap-red/20 font-bold;
}

.badge-neutral {
  @apply bg-cap-grey-200 text-cap-text-muted border border-cap-grey-300;
}

.badge-blue {
  @apply bg-cap-blue/10 text-cap-blue-700 border border-cap-blue/20;
}

.badge-error {
  @apply bg-cap-red/10 text-cap-red border border-cap-red/20;
}

.badge-warning {
  @apply bg-orange-100 text-orange-700 border border-orange-200;
}
```

### Form Elements
```css
.label {
  @apply block text-sm font-medium text-cap-text mb-1;
}

.input {
  @apply px-2.5 py-1.5 border border-cap-grey-500 rounded text-sm
         transition-all duration-200
         focus:outline-none focus:border-cap-blue focus:ring-2 focus:ring-cap-blue/10;
}

.select {
  @apply px-2.5 py-1.5 border border-cap-grey-500 rounded text-sm 
         bg-white transition-all duration-200
         focus:outline-none focus:border-cap-blue focus:ring-2 focus:ring-cap-blue/10;
}

.textarea {
  @apply px-2.5 py-1.5 border border-cap-grey-500 rounded text-sm
         transition-all duration-200
         focus:outline-none focus:border-cap-blue focus:ring-2 focus:ring-cap-blue/10;
}
```

### Loading Spinner
```css
.spinner {
  @apply inline-block w-4 h-4 border-2 border-cap-grey-400 
         border-t-cap-blue rounded-full animate-spin;
}

.spinner-lg {
  @apply inline-block w-8 h-8 border-2 border-cap-grey-400 
         border-t-cap-blue rounded-full animate-spin;
}
```

---

## Shadows

```css
shadow-sm: 0 4px 12px rgba(0, 0, 0, 0.06)
shadow-md: 0 8px 20px rgba(0, 0, 0, 0.06)
shadow-card: 0 8px 20px rgba(0, 0, 0, 0.06)
shadow-card-hover: 0 30px 60px rgba(187, 187, 187, 0.6)
```

---

## Border Radius

```css
rounded-sm: 4px
rounded: 6px
rounded-md: 8px
rounded-card: 10px
rounded-btn: 6px
rounded-panel: 12px
```

---

## Animations

### Fade Up
```css
.animate-fade-up {
  animation: fade-up 0.3s ease-out;
}

@keyframes fade-up {
  0% {
    opacity: 0;
    transform: translateY(30px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}
```

### Transitions
- **Standard:** `transition-colors duration-200`
- **All properties:** `transition-all duration-200`
- **Hover states:** Always 150-200ms

---

## Layout Patterns

### Dashboard Grid
```jsx
<div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
  {/* Stat cards */}
</div>
```

### Two-Column Layout
```jsx
<div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
  {/* Content */}
</div>
```

### Card with Header
```jsx
<div className="card">
  <h3 className="cap-section-title">Title</h3>
  <p className="text-cap-text-muted">Content</p>
</div>
```

---

## Usage Examples

### Page Header
```jsx
<div className="flex items-center justify-between mb-6">
  <h1 className="cap-page-title mb-0">Page Title</h1>
  <button className="btn-primary">Action</button>
</div>
```

### Stat Card
```jsx
<div className="card hover:shadow-card-hover transition-all duration-200">
  <h3 className="cap-section-title">Metric Name</h3>
  <p className="text-4xl font-bold text-cap-deep-blue">1,234</p>
  <p className="text-sm text-cap-text-muted mt-2">Description</p>
</div>
```

### Alert/Info Box
```jsx
<div className="card cap-card-info">
  <p className="text-sm text-cap-text">
    <strong className="text-cap-deep-blue">Note:</strong> Information message
  </p>
</div>
```

---

## Tailwind Configuration

See `fraud-rule-engine-ui/tailwind.config.js` for complete configuration including:
- Custom color extensions
- Font family setup
- Shadow definitions
- Animation keyframes
- Utility class additions

---

## Design Principles

1. **Consistency** - Use utilities, don't reinvent styles
2. **Hierarchy** - Deep blue for important, muted for secondary
3. **Spacing** - Generous padding (p-6) and gaps (gap-6)
4. **Feedback** - All interactive elements have hover states
5. **Performance** - CSS transitions, not JS animations
6. **Accessibility** - Proper contrast ratios, focus states

---

**Reference:** Based on Capitec Bank brand guidelines  
**Maintained by:** UI Development Team
