# FOSS-101 Project Roadmap

## 1. Project Overview

### 1.1 Project name
**FOSS-101**

### 1.2 Product vision
Build a native Android glossary app that helps users learn FOSS, AI, and technology terms in a clear, searchable, beginner-friendly way.

### 1.3 Target users
- Beginners learning open source and AI concepts
- Students and self-learners
- Curious users who want simple explanations
- Developers who want quick reference material

### 1.4 Core value proposition
The app should make it easy to:
- Browse terms
- Explore categories
- Search for terms
- Read simple definitions and fuller explanations
- Expand later into smarter tools and discovery features

---

## 2. Project Principles

These principles guide the order of work:

1. **Build foundation before polish**
2. **Define scope before designing screens**
3. **Use reusable components instead of one-off UI**
4. **Prioritize MVP completion before advanced features**
5. **Keep architecture clean and scalable**
6. **Delay visual polish, icons, and advanced extras until the base app works well**

---

## 3. Version Scope

### 3.1 MVP features
These are the features that belong in the first usable version.

1. Home screen
2. Browse Terms screen
3. Categories screen
4. Search screen
5. Term Details screen
6. Settings screen

### 3.2 Post-MVP features
These should be postponed until the MVP is solid.

1. AI Tools
2. Trend Watcher
3. Ask the Glossary / Chat
4. Favorites / bookmarks
5. History / recently viewed
6. Remote content sync

---

## 4. Recommended Build Order

The project should be executed in this order:

1. Define product and scope
2. Define information architecture
3. Define data model and content structure
4. Define data source strategy
5. Define project architecture and package structure
6. Set up the engineering foundation
7. Plan reusable UI components
8. Plan layouts screen by screen
9. Implement MVP screens
10. Add app logic and interactions
11. Add polish and accessibility improvements
12. Add post-MVP features
13. Test, refactor, and stabilize

---

## 5. Numbered Task Roadmap

## Phase 1 — Product and Scope

### Task 1.1 — Write the product statement
Define the app in one short paragraph.

**Target outcome:**  
A stable product statement that explains what FOSS-101 does and who it is for.

**Suggested result:**  
FOSS-101 is a native Android glossary app that helps users understand FOSS, AI, and technology terms through browsing, categories, search, and clear explanations.

### Task 1.2 — Define the target audience
Write down the primary user groups.

**Checklist:**
- Beginners
- Students / self-learners
- Curious tech users
- Developers needing quick reference

### Task 1.3 — Confirm MVP scope
Lock the first version to these screens only:
- Home
- Browse Terms
- Categories
- Search
- Details
- Settings

**Rule:**  
Do not expand scope until these core flows work well.

### Task 1.4 — Record postponed features
Create a list of features intentionally excluded from the MVP.

**Purpose:**  
This prevents scope creep and keeps development focused.

---

## Phase 2 — Information Architecture

### Task 2.1 — Create the screen map
Document the app’s core screens.

**Required screens:**
1. Home
2. Browse Terms
3. Categories
4. Search
5. Term Details
6. Settings

### Task 2.2 — Define navigation flow
Document how users move between screens.

**Recommended flow:**
- Home → Browse Terms
- Home → Categories
- Home → Search
- Browse Terms → Term Details
- Categories → Filtered Terms → Term Details
- Search → Term Details
- Home or App Bar → Settings

### Task 2.3 — Confirm route naming
Standardize navigation route names.

**Suggested routes:**
- `home`
- `browse`
- `categories`
- `search`
- `details/{termId}`
- `settings`

### Task 2.4 — Separate MVP vs future navigation
Leave non-MVP routes out of the primary navigation until later.

---

## Phase 3 — Data Model and Content

### Task 3.1 — Define the glossary term model
Create a Kotlin data model for glossary terms.

**Recommended fields:**
- `id`
- `term`
- `shortDefinition`
- `fullExplanation`
- `category`
- `tags`
- `relatedTerms`
- `exampleUsage` (optional)
- `source` (optional)

### Task 3.2 — Define the category model
Create a model for categories.

**Recommended fields:**
- `id`
- `name`
- `description`

### Task 3.3 — Decide the initial content source
Use a local data source first.

**Recommended options:**
1. Kotlin hardcoded sample data
2. JSON file in assets/resources

**Recommended choice for now:**  
Start with Kotlin sample data for speed and simplicity.

### Task 3.4 — Create a starter dataset
Create enough entries to test the app meaningfully.

**Suggested initial size:**
- 20 to 30 glossary terms
- 4 to 6 categories

### Task 3.5 — Define content quality rules
Every term should have:
- A clear title
- A short definition
- A fuller explanation
- A valid category

---

## Phase 4 — Architecture and Project Structure

### Task 4.1 — Confirm architectural approach
Use a modern Android structure.

**Recommended stack:**
- Kotlin
- Jetpack Compose
- Navigation Compose
- ViewModel
- Simple repository layer

### Task 4.2 — Define package structure
Create a clean package layout.

**Suggested structure:**
- `data/`
- `model/`
- `ui/`
- `ui/screens/`
- `ui/components/`
- `navigation/`
- `viewmodel/`
- `theme/`

### Task 4.3 — Separate concerns properly
Apply these rules:
- UI composables should focus on presentation
- ViewModels should manage UI state
- Data access should be handled outside composables
- Navigation should remain centralized

### Task 4.4 — Reduce logic inside composables
Avoid putting too much filtering, state mutation, or data creation directly in screen composables.

---

## Phase 5 — Engineering Foundation

### Task 5.1 — Verify dependencies
Confirm the project includes the required dependencies for:
- Compose
- Material 3
- Navigation Compose
- Lifecycle / ViewModel support

### Task 5.2 — Stabilize Gradle setup
Ensure the app builds cleanly before expanding the project.

### Task 5.3 — Confirm theme foundation
Set up the app’s theme properly.

**Theme checklist:**
- Color scheme
- Typography
- Shapes
- Light/dark support readiness

### Task 5.4 — Clean project structure
Remove or reduce unnecessary experimental clutter before expanding the codebase.

### Task 5.5 — Ensure Git hygiene
Keep the repository clean.

**Checklist:**
- `.gitignore` is correct
- local-only files stay ignored
- commit messages stay meaningful

---

## Phase 6 — Reusable UI Components

### Task 6.1 — List reusable components
Before building screens, define the UI building blocks.

**Likely components:**
1. App top bar
2. Primary action button
3. Section header
4. Glossary list item
5. Category card
6. Search bar
7. Empty state view
8. Loading state view
9. Detail content block

### Task 6.2 — Define component responsibilities
Each reusable component should have a single clear purpose.

### Task 6.3 — Standardize shared UI values
Define reusable spacing, sizing, and radius rules.

**Examples:**
- Horizontal padding
- Vertical section spacing
- Card corner radius
- Button height

### Task 6.4 — Create reusable composables first
Do not rebuild button/card logic separately on every screen.

---

## Phase 7 — Layout Planning

This is where layout work begins.

### Task 7.1 — Plan Home screen layout
Define:
- screen hierarchy
- title/subtitle placement
- grouping of primary vs secondary actions
- scroll behavior
- settings access strategy

### Task 7.2 — Plan Browse Terms layout
Define:
- list structure
- header behavior
- scrolling
- term item presentation

### Task 7.3 — Plan Categories layout
Define:
- grid vs list
- category card style
- navigation to filtered content

### Task 7.4 — Plan Search layout
Define:
- search bar placement
- result list behavior
- empty state behavior

### Task 7.5 — Plan Term Details layout
Define:
- term title
- short definition
- full explanation
- related terms section
- category display

### Task 7.6 — Plan Settings layout
Define:
- sections
- preference rows
- simple, clean structure

### Task 7.7 — Define responsive and accessibility rules
Check for:
- small screen support
- larger phone support
- readable text sizing
- touch target sizes

**Important note:**  
Icons belong later in the polish phase, not before the layout plan is complete.

---

## Phase 8 — MVP Screen Implementation

### Task 8.1 — Implement Home screen
Goal: establish the app’s first impression and main navigation.

### Task 8.2 — Implement Browse Terms screen
Goal: display the core glossary content.

### Task 8.3 — Implement Term Details screen
Goal: allow users to open and read full glossary entries.

### Task 8.4 — Implement Categories screen
Goal: allow category-based exploration.

### Task 8.5 — Implement Search screen
Goal: allow users to quickly find terms.

### Task 8.6 — Implement Settings screen
Goal: provide app preferences and utility options.

---

## Phase 9 — App Logic and Interactions

### Task 9.1 — Implement term selection flow
Selecting a term from Browse/Search/Categories should open the Details screen.

### Task 9.2 — Implement search filtering
Search should filter terms clearly and predictably.

### Task 9.3 — Implement category filtering
Categories should open a filtered list or filtered results view.

### Task 9.4 — Improve state handling
Use ViewModel-backed state where appropriate.

### Task 9.5 — Add empty states
Support situations like:
- no search results
- empty category
- missing data

### Task 9.6 — Add loading-state strategy if needed
Especially useful if the app later moves beyond static local data.

---

## Phase 10 — Polish and UX Refinement

### Task 10.1 — Refine typography and spacing
Tune the app’s visual hierarchy after core functionality works.

### Task 10.2 — Add icons
Only after layout and flow are stable.

**Options later:**
- Android Studio vector assets
- Material icons
- custom icons if needed

### Task 10.3 — Improve visual consistency
Check:
- button styles
- card styles
- spacing consistency
- section headers

### Task 10.4 — Add accessibility improvements
Include:
- content descriptions
- readable contrast
- large enough touch targets
- scalable text support

### Task 10.5 — Prepare dark mode quality
Review screens in both light and dark themes if supported.

---

## Phase 11 — Post-MVP Feature Expansion

Only begin this phase after the MVP is stable.

### Task 11.1 — Plan AI Tools
Define whether this is educational content, a launcher, or an interactive utility screen.

### Task 11.2 — Plan Trend Watcher
Define the purpose clearly before implementation.

### Task 11.3 — Plan Ask the Glossary / Chat
Define whether it is static help, smart Q&A, or a future AI-powered feature.

### Task 11.4 — Evaluate bookmarks/favorites
Add only if it provides real value and does not distract from the glossary core.

---

## Phase 12 — Testing, Refactoring, and Stabilization

### Task 12.1 — Test navigation flows
Check every route and back-stack behavior.

### Task 12.2 — Test content flows
Check browse, categories, search, and details together.

### Task 12.3 — Test on different screen sizes
Verify usability on smaller and larger Android phones.

### Task 12.4 — Refactor for cleanliness
Improve:
- naming consistency
- file organization
- reusable logic extraction
- state clarity

### Task 12.5 — Review release readiness
Final checks:
- no obvious crashes
- no broken routes
- no placeholder junk left in UI
- repo remains clean

---

## 6. Immediate Execution Order

These are the next tasks to do in the repo, one by one.

1. Finalize the product statement
2. Lock the MVP feature list
3. Confirm the screen map and route names
4. Define the glossary term model
5. Define the category model
6. Decide the initial local data source
7. Create the starter dataset
8. Review and clean package structure
9. Confirm theme and dependency foundation
10. List reusable components
11. Start layout planning with the Home screen

---

## 7. Definition of Done for MVP

The MVP is considered done when all of the following are true:

1. The app has working Home, Browse, Categories, Search, Details, and Settings screens
2. Users can browse terms and open term details
3. Users can search for terms successfully
4. Users can explore terms by category
5. The app structure is clean and scalable
6. The UI is consistent and usable
7. The project builds and runs cleanly
8. The repository is clean and properly committed

---

## 8. Current Recommended Next Step

**Next step:**  
Start with **Task 1.1 to Task 1.4** and treat them as officially approved project scope.

After that, proceed to:
- Task 2.1 screen map
- Task 2.2 navigation flow
- Task 2.3 route naming

Only after those are settled should layout planning begin.
