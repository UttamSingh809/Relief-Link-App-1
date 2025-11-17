# TODO List for Navbar Fix

## Issues Identified
1. Navbar link arrangement changes when clicking different items due to inconsistent order across templates.
2. Donors can see the "Request" option on the emergency page (and possibly other pages).
3. Inconsistent visibility: some pages have links hidden by default with JS control, others don't.

## Plan
1. Standardize navbar link order across all templates: Dashboard, Donate, Request, Matches, Guidelines, Emergency, Admin, Logout.
2. Set all navbar links to `style="display:none;"` by default in all templates, except Dashboard and Logout which are always visible.
3. Add role-based navbar visibility control to donate.js and request.js.
4. Ensure emergency.js correctly hides requestLink for DONOR role.
5. Update all HTML templates to have consistent navbar structure.

## Tasks
- [x] Update dashboard.html navbar to standard order and hide all links except Dashboard/Logout.
- [x] Update donate.html navbar to standard order and hide all links except Dashboard/Logout.
- [x] Update request.html navbar to standard order and hide all links except Dashboard/Logout.
- [x] Update emergency-contacts.html navbar to standard order (already hidden).
- [x] Update matches.html navbar to standard order (already hidden).
- [x] Update guidelines.html navbar to standard order (already hidden).
- [x] Update admin.html navbar to standard order and hide all links except Dashboard/Logout.
- [x] Add showNavbarLinks function to donate.js and call it on load.
- [x] Add showNavbarLinks function to request.js and call it on load.
- [x] Add showNavbarLinks function to admin.js and call it on load.
- [x] Verify that for DONOR role, requestLink is hidden on all pages.
