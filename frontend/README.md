# Frontend — Java Engineering Lab

Next.js (App Router) + React + TypeScript + Tailwind CSS.

Documentação do projeto como um todo, incluindo como executar o ambiente
completo, está no [README raiz](../README.md) e em
[`specs/architecture/`](../specs/architecture/).

## Desenvolvimento local

```bash
npm install
npm run dev
```

Abra http://localhost:3000 (ou a porta indicada no terminal, se a 3000
estiver em uso).

## Scripts

- `npm run dev` — servidor de desenvolvimento.
- `npm run build` — build de produção (`output: "standalone"`, usado pelo
  `Dockerfile`).
- `npm run lint` — ESLint.
