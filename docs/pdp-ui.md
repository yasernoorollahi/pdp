# PDP UI (React + TypeScript)

This document explains the `pdp-ui` project structure, architecture, and how it connects to the core backend.

Project path:
- `/Users/yaser/Downloads/@PDP/working on pdp with AI tools/pdp-ui`

## Architecture Overview

- **Runtime**: Vite + React 19 + TypeScript
- **Routing**: `react-router-dom` with public, user, and admin route groups
- **Auth state**: Context + localStorage tokens
- **API**: Axios instance with interceptor adding `Authorization: Bearer <token>`
- **UI composition**: Layouts + feature folders + shared UI components

High-level flow:

```
Login -> store tokens -> decode JWT -> build user context
Routes -> RoleGuard -> Layout -> Feature page -> service -> axios -> backend API
```

## Folder Structure

Key folders under `src/`:

- `app/` — App root component
- `router/` — routes and guards
- `features/` — feature pages (auth, insights, moderation, chat, admin, etc.)
- `components/` — shared layout and UI components
- `services/` — client-side API services
- `api/` — axios instance + low-level API wrappers
- `layouts/` — public/admin/user layout shells
- `hooks/` — custom hooks (polling, toast)
- `styles/` — global CSS, variables, theme

## App Entry + Routing

**Entry** (`main.tsx`): wraps the app with `AuthProvider`.

```tsx
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider>
      <App />
    </AuthProvider>
  </StrictMode>,
);
```

**Routing** (`router/index.tsx`):
- Public routes: `/`, `/login`
- User routes: `/app/*` protected by `RoleGuard(['ROLE_USER'])`
- Admin routes: `/admin/*` protected by `RoleGuard(['ROLE_ADMIN'])`

Design decision: roles from JWT are used directly to gate UI access.

## Auth Model

Auth state is centralized in `AuthProvider` and stored in `AuthContext`.

**Flow**
1. `login()` calls `/auth/login`.
2. Stores `accessToken` and `refreshToken` in `localStorage`.
3. Decodes JWT to populate `user` and `roles`.

Snippet:
```tsx
const decoded = jwtDecode<DecodedToken>(res.accessToken);
const newUser: User = { id: 1, email: decoded.sub, roles: decoded.roles };
setUser(newUser);
```

Notes:
- `user.id` is currently hardcoded to `1` because backend does not return it.
- Logout clears localStorage and resets state.

**RoleGuard**
```tsx
const hasAccess = user.roles.some((role) => allowedRoles.includes(role));
if (!hasAccess) return <Navigate to="/" replace />;
```

## API Communication

**Axios instance** (`api/axios.ts`):
- `baseURL` defaults to `http://localhost:8080/api`
- Adds `Authorization: Bearer <token>` for non-auth routes

```ts
api.interceptors.request.use((config) => {
  if (config.url && !config.url.includes('/auth')) {
    const token = localStorage.getItem('accessToken');
    if (token) config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

**Service layer** examples:
- `insights.service.ts` calls `/insights/*` endpoints.
- `chat.service.ts` calls `/user-messages` and normalizes varying API payloads.
- `system.service.ts` calls `/admin/system-overview` for admin dashboards.

## Chat Service (Normalization Logic)

`chat.service.ts` is designed to handle inconsistent payload shapes from the backend.

Key ideas:
- Extract messages from `content`, `items`, `messages`, or `data`.
- Normalize each message to `{id, text, sender, createdAt, status}`.
- Deduplicate by `id + createdAt`.
- Sort by timestamp.

This makes the UI resilient to backend pagination wrappers.

## Layouts and UI Composition

- **PublicLayout**: marketing + login shell.
- **UserLayout**: thin wrapper (ready for user sidebar/header later).
- **AdminLayout**: admin shell with sidebar and main panel.

UI is organized into:
- `components/layout/*` (shells and navigation)
- `components/ui/*` (buttons, cards, tables, skeletons, etc.)

## TypeScript Conventions

- DTO-like types defined close to services (`insights.service.ts`).
- Auth types in `features/auth/types`.
- Shared `User` model in `features/auth/models/User.ts`.

## Integration with PDP Core

- `pdp-ui` talks to the core Spring Boot API at `VITE_API_BASE_URL`.
- Auth and role claims depend on JWT from core.
- Admin dashboards map directly to `/api/admin/*` endpoints.
- Chat + insights map to `/api/user-messages` and `/api/insights/*` endpoints.

## Known gaps / improvement ideas

- `user.id` in UI is hardcoded; better to return ID from `/auth/login` or `/users/me`.
- Refresh token handling is not implemented in the UI (no silent refresh yet).
- RoleGuard is UI-only; backend remains the source of truth for authorization.
