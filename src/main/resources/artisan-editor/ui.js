import { createModuleI18n as D, useProject as k, useItemTexture as _, McText as j, ItemValueInput as L, ActionChainEditor as E, useContentLang as R, textValue as C, projectLangs as S, injectModuleStyles as z } from "@artisan/editor-sdk";
import { jsxs as s, jsx as i, Fragment as I } from "react/jsx-runtime";
import { useState as M } from "react";
const O = {
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
}, q = {
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
}, g = D({ en: O, fr: q }), J = 28, P = 7;
function c(e) {
  return e && typeof e == "object" && !Array.isArray(e) ? e : void 0;
}
function v(e) {
  const a = Number(e.cycle_length);
  return Number.isInteger(a) && a >= 1 ? a : J;
}
function w(e) {
  const a = /* @__PURE__ */ new Map();
  for (const [n, l] of Object.entries(c(e.days) ?? {})) {
    const t = Number(n);
    Number.isInteger(t) && t >= 1 && a.set(t, c(l) ?? {});
  }
  return a;
}
function F(e) {
  const a = v(e), n = w(e), l = c(e.default_day);
  return Array.from({ length: a }, (t, r) => {
    const u = r + 1, y = n.get(u);
    return { day: u, own: y !== void 0, reward: y ?? l };
  });
}
function G(e) {
  const a = v(e);
  return [...w(e).keys()].filter((n) => n > a).sort((n, l) => n - l);
}
function V(e, a, n) {
  const l = Object.entries(c(e.days) ?? {}).filter(([t]) => Number(t) !== a);
  return n !== void 0 && l.push([String(a), n]), l.sort(([t], [r]) => Number(t) - Number(r)), Object.fromEntries(l);
}
function B(e) {
  return e === void 0 ? e : JSON.parse(JSON.stringify(e));
}
function K(e) {
  if (typeof e == "string") return e;
  const a = c(e), n = a?.extends ?? a?.item ?? a?.material;
  return typeof n == "string" ? n : "";
}
function W(e) {
  const a = c(e)?.count;
  return typeof a == "number" && a > 1 ? a : 1;
}
function Y(e) {
  return Array.isArray(e?.actions) ? e.actions.length : 0;
}
function $(e, a, n) {
  const l = c(e)?.title;
  if (typeof l == "string") return l;
  const t = c(l);
  if (!t) return "";
  if (typeof t.key == "string") return n(t.key);
  const r = t[a];
  if (typeof r == "string") return r;
  const u = Object.values(t).find((y) => typeof y == "string");
  return typeof u == "string" ? u : "";
}
function U(e, a) {
  const n = B(e), l = c(n?.icon);
  if (!l) return n;
  for (const t of ["title", "lore"]) {
    const r = c(l[t]);
    r && typeof r.key == "string" && (l[t] = a(r.key));
  }
  return n;
}
function Q({ projectId: e, value: a, set: n, readOnly: l }) {
  const t = g(), r = c(a) ?? {}, u = F(r), y = G(r), p = c(r.default_day), [o, m] = M(null), h = k(e), A = (d) => h ? C(h, d, S(h)[0] ?? "en") : "", x = (d, f) => n(["days"], V(r, d, f));
  return /* @__PURE__ */ s("div", { className: "daily-rewards", children: [
    /* @__PURE__ */ s("div", { className: "daily-toolbar", children: [
      /* @__PURE__ */ s("label", { className: "daily-cycle", children: [
        /* @__PURE__ */ i("span", { className: "field-label", children: t("cycleLength") }),
        /* @__PURE__ */ i(
          "input",
          {
            className: "input daily-cycle-input",
            type: "number",
            min: 1,
            value: v(r),
            disabled: l,
            onChange: (d) => {
              const f = Number.parseInt(d.target.value, 10);
              Number.isInteger(f) && f >= 1 && n(["cycle_length"], f);
            }
          }
        ),
        /* @__PURE__ */ i("span", { className: "daily-faint", children: t("cycleHint") })
      ] }),
      /* @__PURE__ */ s("div", { className: "daily-legend", children: [
        /* @__PURE__ */ i("span", { className: "daily-legend-own", children: t("legendOwn") }),
        /* @__PURE__ */ i("span", { className: "daily-legend-default", children: t("legendDefault") })
      ] })
    ] }),
    y.length > 0 && /* @__PURE__ */ s("div", { className: "daily-outside", role: "alert", children: [
      /* @__PURE__ */ i("strong", { children: t("outsideTitle") }),
      /* @__PURE__ */ i("span", { children: t("outsideHint") }),
      /* @__PURE__ */ i("div", { className: "daily-outside-days", children: y.map((d) => /* @__PURE__ */ s(
        "button",
        {
          type: "button",
          className: "chip",
          disabled: l,
          onClick: () => x(d, void 0),
          children: [
            t("remove", { day: d }),
            " ×"
          ]
        },
        d
      )) })
    ] }),
    /* @__PURE__ */ s("div", { className: "daily-layout", children: [
      /* @__PURE__ */ s("div", { className: "daily-board", children: [
        /* @__PURE__ */ s(
          "button",
          {
            type: "button",
            className: `daily-default${o === "default" ? " is-selected" : ""}`,
            onClick: () => m("default"),
            children: [
              /* @__PURE__ */ i(N, { reward: p }),
              /* @__PURE__ */ s("span", { className: "daily-default-text", children: [
                /* @__PURE__ */ i("span", { className: "daily-default-title", children: t("defaultTitle") }),
                /* @__PURE__ */ i("span", { className: "daily-faint", children: t(p ? "defaultHint" : "defaultNone") })
              ] })
            ]
          }
        ),
        /* @__PURE__ */ i("div", { className: "daily-grid", style: { gridTemplateColumns: `repeat(${P}, minmax(0, 1fr))` }, children: u.map((d) => /* @__PURE__ */ i(
          Z,
          {
            cell: d,
            projectId: e,
            selected: o === d.day,
            onSelect: () => m(d.day)
          },
          d.day
        )) })
      ] }),
      /* @__PURE__ */ i("aside", { className: "daily-panel", children: o === null ? /* @__PURE__ */ i("p", { className: "daily-faint", children: t("pick") }) : o === "default" ? /* @__PURE__ */ i(b, { title: t("defaultTitle"), hint: t(p ? "defaultHint" : "defaultNone"), children: p ? /* @__PURE__ */ s(I, { children: [
        /* @__PURE__ */ i(
          T,
          {
            projectId: e,
            reward: p,
            readOnly: l,
            onChange: (d, f) => n(["default_day", ...d], f)
          }
        ),
        /* @__PURE__ */ i(
          "button",
          {
            type: "button",
            className: "btn btn-ghost btn-sm",
            disabled: l,
            onClick: () => n(["default_day"], void 0),
            children: t("defaultRemove")
          }
        )
      ] }) : /* @__PURE__ */ i(
        "button",
        {
          type: "button",
          className: "btn btn-sm",
          disabled: l,
          onClick: () => n(["default_day"], { icon: { material: "BREAD" }, actions: [] }),
          children: t("defaultAdd")
        }
      ) }) : /* @__PURE__ */ i(
        ee,
        {
          day: o,
          projectId: e,
          own: w(r).get(o),
          fallback: p,
          readOnly: l,
          onCustomize: () => x(o, U(p, A) ?? { icon: { material: "CHEST" }, actions: [] }),
          onReset: () => x(o, void 0),
          onChange: (d, f) => n(["days", X(r, o), ...d], f),
          onEditDefault: () => m("default")
        }
      ) })
    ] })
  ] });
}
function X(e, a) {
  return Object.keys(c(e.days) ?? {}).find((n) => Number(n) === a) ?? String(a);
}
function Z({
  cell: e,
  projectId: a,
  selected: n,
  onSelect: l
}) {
  const t = g(), r = H(a, e.reward?.icon), u = ["daily-cell", e.own ? "is-own" : "is-default", n ? "is-selected" : ""];
  return /* @__PURE__ */ s("button", { type: "button", className: u.join(" "), onClick: l, title: t("day", { day: e.day }), children: [
    /* @__PURE__ */ i("span", { className: "daily-cell-day", children: e.day }),
    /* @__PURE__ */ i(N, { reward: e.reward }),
    e.own && r && /* @__PURE__ */ i("span", { className: "daily-cell-title", children: /* @__PURE__ */ i(j, { source: r, maxLines: 2, align: "center", scale: 1 }) })
  ] });
}
function N({ reward: e }) {
  const a = e?.icon, n = K(a), l = _(n), t = W(a);
  return /* @__PURE__ */ s("span", { className: "daily-slot", children: [
    l ? /* @__PURE__ */ i("img", { src: l, alt: n, draggable: !1 }) : /* @__PURE__ */ i("span", { className: "daily-slot-name", children: n ? n.slice(0, 3) : "—" }),
    t > 1 && /* @__PURE__ */ i("span", { className: "daily-slot-count", children: t })
  ] });
}
function H(e, a) {
  const n = k(e), l = R();
  return $(a, l, (t) => n ? C(n, t, l) : "");
}
function b({ title: e, hint: a, children: n }) {
  return /* @__PURE__ */ s("div", { className: "daily-panel-body", children: [
    /* @__PURE__ */ s("div", { children: [
      /* @__PURE__ */ i("div", { className: "daily-panel-title", children: e }),
      /* @__PURE__ */ i("div", { className: "daily-faint", children: a })
    ] }),
    n
  ] });
}
function ee({
  day: e,
  projectId: a,
  own: n,
  fallback: l,
  readOnly: t,
  onCustomize: r,
  onReset: u,
  onChange: y,
  onEditDefault: p
}) {
  const o = g(), m = H(a, (n ?? l)?.icon);
  if (!n) {
    const h = Y(l);
    return /* @__PURE__ */ s(b, { title: o("day", { day: e }), hint: o("inheritHint"), children: [
      /* @__PURE__ */ s("div", { className: "daily-inherited", children: [
        /* @__PURE__ */ i(N, { reward: l }),
        /* @__PURE__ */ s("span", { className: "daily-inherited-text", children: [
          m ? /* @__PURE__ */ i(j, { source: m, scale: 1 }) : /* @__PURE__ */ i("span", { className: "daily-faint", children: o("defaultNone") }),
          /* @__PURE__ */ i("span", { className: "daily-faint", children: h > 0 ? o("steps", { count: h }) : o("noActions") })
        ] })
      ] }),
      /* @__PURE__ */ i("button", { type: "button", className: "btn btn-sm", disabled: t, onClick: r, children: o("customize") }),
      /* @__PURE__ */ i("button", { type: "button", className: "btn btn-ghost btn-sm", onClick: p, children: o("editDefault") })
    ] });
  }
  return /* @__PURE__ */ s(b, { title: o("day", { day: e }), hint: o("ownHint"), children: [
    /* @__PURE__ */ i(T, { projectId: a, reward: n, readOnly: t, onChange: y }),
    /* @__PURE__ */ i("button", { type: "button", className: "btn btn-ghost btn-sm", disabled: t, onClick: u, children: o("reset") })
  ] });
}
function T({
  projectId: e,
  reward: a,
  readOnly: n,
  onChange: l
}) {
  const t = g();
  return /* @__PURE__ */ s("fieldset", { className: "daily-fields", disabled: n, children: [
    /* @__PURE__ */ s("div", { children: [
      /* @__PURE__ */ i("div", { className: "field-label", children: t("icon") }),
      /* @__PURE__ */ i("div", { className: "daily-faint", children: t("iconHint") }),
      /* @__PURE__ */ i(L, { projectId: e, value: a.icon, onChange: (r) => l(["icon"], r) })
    ] }),
    /* @__PURE__ */ s("div", { children: [
      /* @__PURE__ */ i("div", { className: "field-label", children: t("actions") }),
      /* @__PURE__ */ i("div", { className: "daily-faint", children: t("actionsHint") }),
      /* @__PURE__ */ i(
        E,
        {
          projectId: e,
          value: Array.isArray(a.actions) ? a.actions : [],
          onChange: (r) => l(["actions"], r)
        }
      )
    ] })
  ] });
}
const te = `
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
z("daily", te);
const ae = {
  id: "daily",
  displayName: "Daily rewards",
  schemaVersion: "daily/v1",
  sidebar: { icon: () => null, label: "Daily rewards", to: "/m/daily" },
  routes: [],
  screens: { rewards: Q }
}, re = { ...ae, sdk: "1.20.0" };
export {
  re as default
};
