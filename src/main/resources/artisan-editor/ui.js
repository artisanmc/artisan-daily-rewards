import { createModuleI18n as z, useProject as T, useItemTexture as U, McText as A, ItemValueInput as O, ActionChainEditor as P, useContentLang as I, textValue as j, projectLangs as F, injectModuleStyles as $ } from "@artisan/editor-sdk";
import { jsxs as d, jsx as s, Fragment as q } from "react/jsx-runtime";
import { useState as J } from "react";
const W = 28, Y = 7;
function f(e) {
  return e && typeof e == "object" && !Array.isArray(e) ? e : void 0;
}
function b(e) {
  const t = Number(e.cycle_length);
  return Number.isInteger(t) && t >= 1 ? t : W;
}
function w(e) {
  const t = /* @__PURE__ */ new Map();
  for (const [a, i] of Object.entries(f(e.days) ?? {})) {
    const n = Number(a);
    Number.isInteger(n) && n >= 1 && t.set(n, f(i) ?? {});
  }
  return t;
}
function G(e) {
  const t = b(e), a = w(e), i = f(e.default_day);
  return Array.from({ length: t }, (n, r) => {
    const l = r + 1, u = a.get(l);
    return { day: l, own: u !== void 0, reward: u ?? i };
  });
}
function V(e) {
  const t = b(e);
  return [...w(e).keys()].filter((a) => a > t).sort((a, i) => a - i);
}
function B(e, t, a) {
  const i = Object.entries(f(e.days) ?? {}).filter(([n]) => Number(n) !== t);
  return a !== void 0 && i.push([String(t), a]), i.sort(([n], [r]) => Number(n) - Number(r)), Object.fromEntries(i);
}
function K(e) {
  return e === void 0 ? e : JSON.parse(JSON.stringify(e));
}
function X(e) {
  if (typeof e == "string") return e;
  const t = f(e), a = t?.extends ?? t?.item ?? t?.material;
  return typeof a == "string" ? a : "";
}
function Q(e) {
  const t = f(e)?.count;
  return typeof t == "number" && t > 1 ? t : 1;
}
function Z(e) {
  return Array.isArray(e?.actions) ? e.actions.length : 0;
}
function ee(e, t, a) {
  const i = f(e)?.title;
  if (typeof i == "string") return i;
  const n = f(i);
  if (!n) return "";
  if (typeof n.key == "string") return a(n.key);
  const r = n[t];
  if (typeof r == "string") return r;
  const l = Object.values(n).find((u) => typeof u == "string");
  return typeof l == "string" ? l : "";
}
function te(e, t) {
  const a = K(e), i = f(a?.icon);
  if (!i) return a;
  for (const n of ["title", "lore"]) {
    const r = f(i[n]);
    r && typeof r.key == "string" && (i[n] = t(r.key));
  }
  return a;
}
const ae = "daily:calendar", D = "daily:status", ne = "rewards", ie = 7, se = 20, re = 35, S = { material: "PAPER" }, le = 864e5;
function R(e) {
  const t = e?.dataSources?.find(
    (a) => String(a.id) === ne
  );
  if (!(!t || t.shape !== "config"))
    return f(t.value);
}
function oe(e) {
  if (R(e)) return !0;
  const t = e;
  return t?.ownerModule === "daily" || String(t?.id ?? "") === "daily" || String(t?.id ?? "").endsWith(":daily");
}
function x(e, t) {
  return e < 1 ? 1 : (e - 1) % t + 1;
}
function H(e, t) {
  if (!(t < 1))
    return w(e).get(t) ?? f(e.default_day);
}
function C(e, t, a) {
  const i = e?.[t];
  if (i === void 0) return a;
  const n = Number.parseInt(String(i).trim(), 10);
  return Number.isNaN(n) ? a : n;
}
function de(e, t, a) {
  const i = [
    { status: "claimed", streak: 1 },
    { status: "claimed", streak: 2 },
    { status: "claimed", streak: 3 },
    { status: "missed", streak: 0 },
    { status: "available", streak: 4 }
  ], n = [];
  for (let l = -e; l < 0; l++) {
    const u = i[i.length + l];
    n.push(
      u ? { offset: l, status: u.status, cycleDay: u.streak > 0 ? x(u.streak, a) : 0 } : { offset: l, status: "before", cycleDay: 0 }
    );
  }
  const r = 5;
  n.push({ offset: 0, status: "available", cycleDay: x(r, a) });
  for (let l = 1; l <= t; l++)
    n.push({ offset: l, status: "upcoming", cycleDay: x(r + l, a) });
  return n;
}
function g(e) {
  return String(e).padStart(2, "0");
}
function ce(e, t, a) {
  const i = new Date(Date.UTC(a.getFullYear(), a.getMonth(), a.getDate()) + e.offset * le), n = H(t, e.cycleDay);
  return {
    id: `${i.getUTCFullYear()}-${g(i.getUTCMonth() + 1)}-${g(i.getUTCDate())}`,
    date: `${g(i.getUTCDate())}/${g(i.getUTCMonth() + 1)}`,
    offset: e.offset,
    status: e.status,
    is_today: e.offset === 0,
    is_claimed: e.status === "claimed",
    is_missed: e.status === "missed",
    is_before: e.status === "before",
    is_available: e.status === "available",
    is_upcoming: e.status === "upcoming",
    cycle_day: e.cycleDay,
    has_reward: n !== void 0,
    icon: n?.icon ?? S
  };
}
function ue(e, t) {
  const a = H(t, Number(e.cycle_day) || 0);
  return { ...e, has_reward: a !== void 0, icon: a?.icon ?? S };
}
function fe(e) {
  return {
    id: "status",
    loaded: !0,
    streak: 4,
    longest: 12,
    total: 31,
    freezes: 0,
    freeze_max: 3,
    claimed_today: !1,
    unclaimed: 2,
    can_claim: !0,
    at_risk: !1,
    broken: !1,
    lost: 0,
    cycle_day: x(5, e),
    cycle_length: e,
    until_reset: "3h 12m"
  };
}
function ye(e, t, a, i = /* @__PURE__ */ new Date()) {
  if (t !== ae && t !== D || !oe(e)) return null;
  const n = R(e) ?? {}, r = b(n), l = a?.serverRows;
  if (t === D) {
    const o = l?.filter((p) => p.loaded === !0) ?? [];
    return o.length > 0 ? o.map((p) => ({ ...p, cycle_length: r })) : [fe(r)];
  }
  if (l && l.length > 0) return l.map((o) => ue(o, n));
  const u = Math.max(0, Math.min(re, C(a?.params, "past", ie))), y = Math.max(0, C(a?.params, "future", se));
  return de(u, y, r).map((o) => ce(o, n, i));
}
const pe = {
  cycleLength: "Cycle length",
  cycleHint: "Days before the rewards start again at day 1. The streak goes on.",
  days: "{{count}} days",
  legendOwn: "Its own reward",
  legendDefault: "Reward of the other days",
  defaultTitle: "The other days",
  defaultHint: "What a day without its own reward pays.",
  defaultNone: "None: no reward on those days.",
  defaultAdd: "Add a reward for the other days",
  defaultRemove: "No reward on the other days",
  day: "Day {{day}}",
  ownHint: "Its own reward, in place of the other days' one.",
  inheritHint: "This day pays the reward of the other days.",
  customize: "Give this day its own reward",
  editDefault: "Edit the reward of the other days",
  reset: "Back to the reward of the other days",
  icon: "Icon",
  iconHint: "Shown in the /daily menu: name it after the reward.",
  actions: "Actions",
  actionsHint: "Run when the day is claimed: give_item, give_money, a command… ({streak}, {cycle_day}, {cycle_length}).",
  steps: "{{count}} step(s)",
  noActions: "no action",
  outsideTitle: "Days past the cycle",
  outsideHint: "The plugin refuses the file while these days are listed: lengthen the cycle or remove them.",
  remove: "Remove day {{day}}",
  pick: "Click a day to edit its reward."
}, me = {
  cycleLength: "Longueur du cycle",
  cycleHint: "Jours avant que les récompenses reprennent au jour 1. Le streak continue.",
  days: "{{count}} jours",
  legendOwn: "Récompense propre",
  legendDefault: "Récompense des autres jours",
  defaultTitle: "Les autres jours",
  defaultHint: "Ce que rapporte un jour sans récompense propre.",
  defaultNone: "Aucune : pas de récompense ces jours-là.",
  defaultAdd: "Ajouter une récompense pour les autres jours",
  defaultRemove: "Pas de récompense les autres jours",
  day: "Jour {{day}}",
  ownHint: "Sa propre récompense, à la place de celle des autres jours.",
  inheritHint: "Ce jour rapporte la récompense des autres jours.",
  customize: "Donner à ce jour sa propre récompense",
  editDefault: "Modifier la récompense des autres jours",
  reset: "Revenir à la récompense des autres jours",
  icon: "Icône",
  iconHint: "Affichée dans le menu /daily : nomme-la d'après la récompense.",
  actions: "Actions",
  actionsHint: "Exécutées quand le jour est réclamé : give_item, give_money, une commande… ({streak}, {cycle_day}, {cycle_length}).",
  steps: "{{count}} étape(s)",
  noActions: "aucune action",
  outsideTitle: "Jours hors du cycle",
  outsideHint: "Le plugin refuse le fichier tant que ces jours y sont : allonge le cycle ou retire-les.",
  remove: "Retirer le jour {{day}}",
  pick: "Clique sur un jour pour modifier sa récompense."
}, v = z({ en: pe, fr: me });
function he({ projectId: e, value: t, set: a, readOnly: i }) {
  const n = v(), r = f(t) ?? {}, l = G(r), u = V(r), y = f(r.default_day), [o, p] = J(null), h = T(e), M = (c) => h ? j(h, c, F(h)[0] ?? "en") : "", N = (c, m) => a(["days"], B(r, c, m));
  return /* @__PURE__ */ d("div", { className: "daily-rewards", children: [
    /* @__PURE__ */ d("div", { className: "daily-toolbar", children: [
      /* @__PURE__ */ d("label", { className: "daily-cycle", children: [
        /* @__PURE__ */ s("span", { className: "field-label", children: n("cycleLength") }),
        /* @__PURE__ */ s(
          "input",
          {
            className: "input daily-cycle-input",
            type: "number",
            min: 1,
            value: b(r),
            disabled: i,
            onChange: (c) => {
              const m = Number.parseInt(c.target.value, 10);
              Number.isInteger(m) && m >= 1 && a(["cycle_length"], m);
            }
          }
        ),
        /* @__PURE__ */ s("span", { className: "daily-faint", children: n("cycleHint") })
      ] }),
      /* @__PURE__ */ d("div", { className: "daily-legend", children: [
        /* @__PURE__ */ s("span", { className: "daily-legend-own", children: n("legendOwn") }),
        /* @__PURE__ */ s("span", { className: "daily-legend-default", children: n("legendDefault") })
      ] })
    ] }),
    u.length > 0 && /* @__PURE__ */ d("div", { className: "daily-outside", role: "alert", children: [
      /* @__PURE__ */ s("strong", { children: n("outsideTitle") }),
      /* @__PURE__ */ s("span", { children: n("outsideHint") }),
      /* @__PURE__ */ s("div", { className: "daily-outside-days", children: u.map((c) => /* @__PURE__ */ d(
        "button",
        {
          type: "button",
          className: "chip",
          disabled: i,
          onClick: () => N(c, void 0),
          children: [
            n("remove", { day: c }),
            " ×"
          ]
        },
        c
      )) })
    ] }),
    /* @__PURE__ */ d("div", { className: "daily-layout", children: [
      /* @__PURE__ */ d("div", { className: "daily-board", children: [
        /* @__PURE__ */ d(
          "button",
          {
            type: "button",
            className: `daily-default${o === "default" ? " is-selected" : ""}`,
            onClick: () => p("default"),
            children: [
              /* @__PURE__ */ s(k, { reward: y }),
              /* @__PURE__ */ d("span", { className: "daily-default-text", children: [
                /* @__PURE__ */ s("span", { className: "daily-default-title", children: n("defaultTitle") }),
                /* @__PURE__ */ s("span", { className: "daily-faint", children: n(y ? "defaultHint" : "defaultNone") })
              ] })
            ]
          }
        ),
        /* @__PURE__ */ s("div", { className: "daily-grid", style: { gridTemplateColumns: `repeat(${Y}, minmax(0, 1fr))` }, children: l.map((c) => /* @__PURE__ */ s(
          xe,
          {
            cell: c,
            projectId: e,
            selected: o === c.day,
            onSelect: () => p(c.day)
          },
          c.day
        )) })
      ] }),
      /* @__PURE__ */ s("aside", { className: "daily-panel", children: o === null ? /* @__PURE__ */ s("p", { className: "daily-faint", children: n("pick") }) : o === "default" ? /* @__PURE__ */ s(_, { title: n("defaultTitle"), hint: n(y ? "defaultHint" : "defaultNone"), children: y ? /* @__PURE__ */ d(q, { children: [
        /* @__PURE__ */ s(
          L,
          {
            projectId: e,
            reward: y,
            readOnly: i,
            onChange: (c, m) => a(["default_day", ...c], m)
          }
        ),
        /* @__PURE__ */ s(
          "button",
          {
            type: "button",
            className: "btn btn-ghost btn-sm",
            disabled: i,
            onClick: () => a(["default_day"], void 0),
            children: n("defaultRemove")
          }
        )
      ] }) : /* @__PURE__ */ s(
        "button",
        {
          type: "button",
          className: "btn btn-sm",
          disabled: i,
          onClick: () => a(["default_day"], { icon: { material: "BREAD" }, actions: [] }),
          children: n("defaultAdd")
        }
      ) }) : /* @__PURE__ */ s(
        be,
        {
          day: o,
          projectId: e,
          own: w(r).get(o),
          fallback: y,
          readOnly: i,
          onCustomize: () => N(o, te(y, M) ?? { icon: { material: "CHEST" }, actions: [] }),
          onReset: () => N(o, void 0),
          onChange: (c, m) => a(["days", ge(r, o), ...c], m),
          onEditDefault: () => p("default")
        }
      ) })
    ] })
  ] });
}
function ge(e, t) {
  return Object.keys(f(e.days) ?? {}).find((a) => Number(a) === t) ?? String(t);
}
function xe({
  cell: e,
  projectId: t,
  selected: a,
  onSelect: i
}) {
  const n = v(), r = E(t, e.reward?.icon), l = ["daily-cell", e.own ? "is-own" : "is-default", a ? "is-selected" : ""];
  return /* @__PURE__ */ d("button", { type: "button", className: l.join(" "), onClick: i, title: n("day", { day: e.day }), children: [
    /* @__PURE__ */ s("span", { className: "daily-cell-day", children: e.day }),
    /* @__PURE__ */ s(k, { reward: e.reward }),
    e.own && r && /* @__PURE__ */ s("span", { className: "daily-cell-title", children: /* @__PURE__ */ s(A, { source: r, maxLines: 2, align: "center", scale: 1 }) })
  ] });
}
function k({ reward: e }) {
  const t = e?.icon, a = X(t), i = U(a), n = Q(t);
  return /* @__PURE__ */ d("span", { className: "daily-slot", children: [
    i ? /* @__PURE__ */ s("img", { src: i, alt: a, draggable: !1 }) : /* @__PURE__ */ s("span", { className: "daily-slot-name", children: a ? a.slice(0, 3) : "—" }),
    n > 1 && /* @__PURE__ */ s("span", { className: "daily-slot-count", children: n })
  ] });
}
function E(e, t) {
  const a = T(e), i = I();
  return ee(t, i, (n) => a ? j(a, n, i) : "");
}
function _({ title: e, hint: t, children: a }) {
  return /* @__PURE__ */ d("div", { className: "daily-panel-body", children: [
    /* @__PURE__ */ d("div", { children: [
      /* @__PURE__ */ s("div", { className: "daily-panel-title", children: e }),
      /* @__PURE__ */ s("div", { className: "daily-faint", children: t })
    ] }),
    a
  ] });
}
function be({
  day: e,
  projectId: t,
  own: a,
  fallback: i,
  readOnly: n,
  onCustomize: r,
  onReset: l,
  onChange: u,
  onEditDefault: y
}) {
  const o = v(), p = E(t, (a ?? i)?.icon);
  if (!a) {
    const h = Z(i);
    return /* @__PURE__ */ d(_, { title: o("day", { day: e }), hint: o("inheritHint"), children: [
      /* @__PURE__ */ d("div", { className: "daily-inherited", children: [
        /* @__PURE__ */ s(k, { reward: i }),
        /* @__PURE__ */ d("span", { className: "daily-inherited-text", children: [
          p ? /* @__PURE__ */ s(A, { source: p, scale: 1 }) : /* @__PURE__ */ s("span", { className: "daily-faint", children: o("defaultNone") }),
          /* @__PURE__ */ s("span", { className: "daily-faint", children: h > 0 ? o("steps", { count: h }) : o("noActions") })
        ] })
      ] }),
      /* @__PURE__ */ s("button", { type: "button", className: "btn btn-sm", disabled: n, onClick: r, children: o("customize") }),
      /* @__PURE__ */ s("button", { type: "button", className: "btn btn-ghost btn-sm", onClick: y, children: o("editDefault") })
    ] });
  }
  return /* @__PURE__ */ d(_, { title: o("day", { day: e }), hint: o("ownHint"), children: [
    /* @__PURE__ */ s(L, { projectId: t, reward: a, readOnly: n, onChange: u }),
    /* @__PURE__ */ s("button", { type: "button", className: "btn btn-ghost btn-sm", disabled: n, onClick: l, children: o("reset") })
  ] });
}
function L({
  projectId: e,
  reward: t,
  readOnly: a,
  onChange: i
}) {
  const n = v();
  return /* @__PURE__ */ d("fieldset", { className: "daily-fields", disabled: a, children: [
    /* @__PURE__ */ d("div", { children: [
      /* @__PURE__ */ s("div", { className: "field-label", children: n("icon") }),
      /* @__PURE__ */ s("div", { className: "daily-faint", children: n("iconHint") }),
      /* @__PURE__ */ s(O, { projectId: e, value: t.icon, onChange: (r) => i(["icon"], r) })
    ] }),
    /* @__PURE__ */ d("div", { children: [
      /* @__PURE__ */ s("div", { className: "field-label", children: n("actions") }),
      /* @__PURE__ */ s("div", { className: "daily-faint", children: n("actionsHint") }),
      /* @__PURE__ */ s(
        P,
        {
          projectId: e,
          value: Array.isArray(t.actions) ? t.actions : [],
          onChange: (r) => i(["actions"], r)
        }
      )
    ] })
  ] });
}
const we = `
.daily-rewards { display: flex; flex-direction: column; gap: 16px; }
.daily-toolbar { display: flex; flex-wrap: wrap; align-items: flex-end; justify-content: space-between; gap: 12px; }
.daily-cycle { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; }
.daily-cycle-input { width: 80px; }
.daily-faint { color: var(--text-faint); font-size: 12px; line-height: 1.4; }
.daily-legend { display: flex; gap: 12px; font-size: 12px; color: var(--text-mut); }
.daily-legend span { display: inline-flex; align-items: center; gap: 6px; }
.daily-legend span::before { content: ""; width: 12px; height: 12px; border-radius: 3px; }
.daily-legend-own::before { background: var(--fam-reward-soft); border: 2px solid var(--fam-reward); }
.daily-legend-default::before { background: var(--surface-inset); border: 1px solid var(--border-soft); opacity: 0.6; }
.daily-outside { display: flex; flex-direction: column; gap: 6px; padding: 10px 12px; border-radius: var(--r-md);
  background: var(--warning-soft); border: 1px solid var(--warning); color: var(--text); font-size: 13px; }
.daily-outside-days { display: flex; flex-wrap: wrap; gap: 6px; }
.daily-layout { display: grid; grid-template-columns: minmax(0, 1fr) minmax(280px, 380px); gap: 16px; align-items: start; }
@media (max-width: 900px) { .daily-layout { grid-template-columns: minmax(0, 1fr); } }
.daily-board { display: flex; flex-direction: column; gap: 12px; min-width: 0; }
.daily-default { display: flex; align-items: center; gap: 12px; padding: 10px 12px; text-align: left; cursor: pointer;
  border-radius: var(--r-md); border: 1px dashed var(--border); background: var(--surface); color: var(--text); }
.daily-default:hover { border-color: var(--brand-line); }
.daily-default-text { display: flex; flex-direction: column; gap: 2px; }
.daily-default-title { font-weight: 600; font-size: 13px; }
.daily-grid { display: grid; gap: 6px; }
.daily-cell { position: relative; display: flex; flex-direction: column; align-items: center; justify-content: flex-start;
  gap: 4px; min-height: 92px; padding: 18px 4px 6px; cursor: pointer; overflow: hidden;
  border-radius: var(--r-md); border: 1px solid var(--border-soft); background: var(--surface-inset); color: var(--text); }
.daily-cell:hover { border-color: var(--brand-line); }
.daily-cell.is-default .daily-slot { opacity: 0.45; filter: grayscale(0.6); }
.daily-cell.is-own { border: 2px solid var(--fam-reward); background: var(--fam-reward-soft); }
.daily-cell.is-selected, .daily-default.is-selected { outline: 2px solid var(--brand); outline-offset: 1px; }
.daily-cell-day { position: absolute; top: 4px; left: 6px; font-size: 11px; font-weight: 600; color: var(--text-mut); }
.daily-cell.is-own .daily-cell-day { color: var(--text); }
.daily-cell-title { max-width: 100%; font-size: 10px; text-align: center; }
.daily-slot { position: relative; display: inline-flex; align-items: center; justify-content: center; flex: none;
  width: 40px; height: 40px; border-radius: var(--r-xs); background: var(--mc-slot-bg);
  box-shadow: inset 2px 2px 0 var(--mc-slot-dark), inset -2px -2px 0 var(--mc-slot-light); }
.daily-slot img { width: 32px; height: 32px; object-fit: contain; image-rendering: pixelated; }
.daily-slot-name { font-size: 10px; color: var(--text-faint); }
.daily-slot-count { position: absolute; right: 2px; bottom: 0; font-size: 12px; font-weight: 700; color: var(--text);
  text-shadow: 1px 1px 0 var(--background); }
.daily-panel { position: sticky; top: 0; padding: 14px; border-radius: var(--r-lg); border: 1px solid var(--border-soft);
  background: var(--surface); }
.daily-panel-body { display: flex; flex-direction: column; gap: 14px; align-items: stretch; }
.daily-panel-title { font-weight: 600; font-size: 15px; }
.daily-fields { display: flex; flex-direction: column; gap: 14px; border: 0; padding: 0; margin: 0; min-width: 0; }
.daily-fields > div { display: flex; flex-direction: column; gap: 6px; }
.daily-inherited { display: flex; align-items: center; gap: 10px; padding: 10px; border-radius: var(--r-md);
  background: var(--surface-inset); }
.daily-inherited-text { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
`;
$("daily", we);
const ve = {
  id: "daily",
  displayName: "Daily rewards",
  schemaVersion: "daily/v1",
  sidebar: { icon: () => null, label: "Daily rewards", to: "/m/daily" },
  routes: [],
  screens: { rewards: he },
  // The calendar menu previewed in the editor: its days, from the rewards draft.
  localPreviewRows: ye
}, De = { ...ve, sdk: "1.20.0" };
export {
  De as default
};
