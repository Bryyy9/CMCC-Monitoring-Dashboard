---
name: Operational Command
colors:
  surface: '#f8f9ff'
  surface-dim: '#d4dae7'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eef4ff'
  surface-container: '#e7eefb'
  surface-container-high: '#e2e8f5'
  surface-container-highest: '#dce3f0'
  on-surface: '#151c25'
  on-surface-variant: '#45464c'
  inverse-surface: '#2a313b'
  inverse-on-surface: '#eaf1fe'
  outline: '#76777d'
  outline-variant: '#c6c6cd'
  surface-tint: '#575e70'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#141b2b'
  on-primary-container: '#7d8497'
  inverse-primary: '#c0c6db'
  secondary: '#006e2f'
  on-secondary: '#ffffff'
  secondary-container: '#6bff8f'
  on-secondary-container: '#007432'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#410004'
  on-tertiary-container: '#ef4444'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dce2f7'
  primary-fixed-dim: '#c0c6db'
  on-primary-fixed: '#141b2b'
  on-primary-fixed-variant: '#404758'
  secondary-fixed: '#6bff8f'
  secondary-fixed-dim: '#4ae176'
  on-secondary-fixed: '#002109'
  on-secondary-fixed-variant: '#005321'
  tertiary-fixed: '#ffdad7'
  tertiary-fixed-dim: '#ffb3ad'
  on-tertiary-fixed: '#410004'
  on-tertiary-fixed-variant: '#930013'
  background: '#f8f9ff'
  on-background: '#151c25'
  surface-variant: '#dce3f0'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  data-lg:
    fontFamily: JetBrains Mono
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  data-md:
    fontFamily: JetBrains Mono
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
  label-caps:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  unit: 4px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  gutter: 16px
  margin-mobile: 16px
  margin-desktop: 32px
---

## Brand & Style

The design system is engineered for a Centralized Monitoring Command Center (CMCC), prioritizing immediate situational awareness and high data density. The brand personality is authoritative, precise, and urgent without being chaotic. It is designed for Site Reliability Engineers (SREs) and system administrators who require a "single pane of glass" view to identify and resolve infrastructure issues rapidly.

The visual style is **High-Contrast / Modern**, leaning into a functional aesthetic that avoids decorative flourishes in favor of information clarity. It utilizes sharp contrast, systematic spacing, and clear state-based signaling to ensure that critical failures (DOWN states) are visually inescapable while healthy states remain supportive and unobtrusive. The interface maintains a professional, institutional feel that inspires confidence during high-pressure incidents.

## Colors

The palette is strictly functional, mapping color directly to system health and priority.

- **Primary (#111827):** Used for typography, iconography, and structural elements to provide a grounded, high-contrast framework.
- **Success/UP (#22c55e):** A vibrant green used for healthy status indicators and positive trends.
- **Error/DOWN (#ef4444):** An urgent red reserved exclusively for critical failures and active alerts.
- **Neutral/Unknown (#9ca3af):** A medium gray for inactive states, unknown statuses, or secondary metadata.
- **Surface (#f9fafb):** A clean, cool-toned white background to minimize eye strain during long shifts while ensuring foreground elements pop.

## Typography

This design system utilizes a dual-font approach to maximize readability and data alignment.

1.  **Inter (Proportional):** Used for all UI labels, navigation, and general content. It provides excellent legibility at small sizes and a modern, neutral tone.
2.  **JetBrains Mono (Monospaced):** Used exclusively for technical data, including latency values (ms), timestamps, IP addresses, and status counts. The fixed width ensures that columns of numbers align vertically, allowing users to scan for anomalies across rows instantly.

**Scale and Usage:**
- **Display:** Large counters in summary cards.
- **Data Roles:** All real-time metrics use the `data-` roles to prevent "shimmering" or layout shifts when numbers change.
- **Label Caps:** Used for section headers and table column headers to create a clear structural hierarchy.

## Layout & Spacing

The layout is based on a **12-column fluid grid** designed for 24/7 monitoring displays. 

- **Density:** High. Padding is kept tight to maximize the amount of information visible without scrolling.
- **Grid:** Use a 16px gutter between cards. Components should snap to the grid.
- **Responsive Behavior:** 
    - **Desktop (1440px+):** Full dashboard view with side navigation.
    - **Tablet (768px - 1439px):** Reflows into a 2-column card layout; side navigation collapses to icons.
    - **Mobile (<767px):** Single column stack. Summary metrics remain at the top.
- **Rhythm:** All margins and paddings are multiples of 4px to maintain a strict geometric alignment.

## Elevation & Depth

This design system uses a **Tonal Layering** approach with **Low-Contrast Outlines** instead of heavy shadows to maintain a clean, professional "glass-cockpit" feel.

- **Level 0 (Background):** #f9fafb.
- **Level 1 (Cards/Containers):** Solid white (#ffffff) with a 1px border (#e5e7eb). No shadow.
- **Level 2 (Active/Hover):** Solid white with a 1px border (#d1d5db) and a very subtle, tight ambient shadow (4px blur, 2% opacity).
- **Critical Elevation:** Elements in a **DOWN** state do not use depth to stand out; instead, they use high-saturation color fills and a 2px stroke of #ef4444 to break the layout rhythm and command attention.

## Shapes

The shape language is **Soft (0.25rem)**, providing a disciplined, industrial look that is slightly more approachable than sharp corners.

- **Small Components:** Checkboxes, status badges, and small buttons use the base `rounded` (4px).
- **Large Components:** Summary cards and service containers use `rounded-lg` (8px).
- **Interactive Elements:** Action buttons maintain the 4px radius to feel like physical hardware buttons.
- **Exceptions:** Status "pills" or badges may use a full pill shape (999px) to distinguish them from structural card elements.

## Components

### Summary Cards
Large-format cards appearing at the top of the dashboard. They feature a `display-lg` counter (Monospace) and a `label-caps` description. Trend indicators (arrows) are placed adjacent to the counter.

### Service Cards
The primary unit of the dashboard.
- **Header:** Service name (Headline-sm) and a status badge.
- **Body:** Latency and Uptime strings in `data-md` typography.
- **Visual Signal:** When status is `DOWN`, the card border thickens to 2px #ef4444 and the status badge performs a "Pulse" animation (scaling from 1.0 to 1.1 with a soft glow).

### Status Badges
- **UP:** Green background (10% opacity) with Green text.
- **DOWN:** Solid Red background with White text.
- **UNKNOWN:** Gray background (10% opacity) with Gray text.

### Error Banners
Full-width alerts that appear at the top of the content area. They use a solid #ef4444 left-border (4px) and a light red tint background to ensure they cannot be scrolled past without notice.

### Input Fields & Buttons
- **Fields:** Subtle gray borders (#d1d5db) that turn #111827 on focus.
- **Buttons:** Primary buttons are #111827 with white text. Secondary buttons are outlined.

### Skeleton Loaders
Use a subtle shimmer animation across gray blocks (#f3f4f6) that match the exact height and width of the data they replace to prevent layout jumping.