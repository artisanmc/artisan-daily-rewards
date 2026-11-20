import { createModuleI18n as se, useProject as W, useItemTexture as le, McText as I, ItemValueInput as re, ActionChainEditor as oe, useContentLang as de, textValue as J, projectLangs as ce, injectModuleStyles as ue } from "@artisan/editor-sdk";
import { jsxs as d, jsx as s, Fragment as V } from "react/jsx-runtime";
import { useState as me } from "react";
const pe = 28, fe = 7, ye = "with_default_day";
function F(e) {
  return e.with_default_day === !0;
}
function G(e, t) {
  const n = t[ye];
  return typeof n == "boolean" ? n : F(e);
}
function y(e) {
  return e && typeof e == "object" && !Array.isArray(e) ? e : void 0;
}
function D(e) {
  const t = Number(e.cycle_length);
  return Number.isInteger(t) && t >= 1 ? t : pe;
}
function j(e) {
  const t = /* @__PURE__ */ new Map();
  for (const [n, i] of Object.entries(y(e.days) ?? {})) {
    const a = Number(n);
    Number.isInteger(a) && a >= 1 && t.set(a, y(i) ?? {});
  }
  return t;
}
function he(e) {
  const t = D(e), n = j(e), i = y(e.default_day);
  return Array.from({ length: t }, (a, l) => {
    const o = l + 1, r = n.get(o), f = r !== void 0 && i !== void 0 && G(e, r);
    return { day: o, own: r !== void 0, reward: r ?? i, stacks: f };
  });
}
function ge(e) {
  const t = D(e);
  return [...j(e).keys()].filter((n) => n > t).sort((n, i) => n - i);
}
function be(e, t, n) {
  const i = Object.entries(y(e.days) ?? {}).filter(([a]) => Number(a) !== t);
  return n !== void 0 && i.push([String(t), n]), i.sort(([a], [l]) => Number(a) - Number(l)), Object.fromEntries(i);
}
function ve(e) {
  return e === void 0 ? e : JSON.parse(JSON.stringify(e));
}
function xe(e) {
  if (typeof e == "string") return e;
  const t = y(e), n = t?.extends ?? t?.item ?? t?.material;
  return typeof n == "string" ? n : "";
}
function we(e) {
  const t = y(e)?.count;
  return typeof t == "number" && t > 1 ? t : 1;
}
function Ne(e) {
  return Array.isArray(e?.actions) ? e.actions.length : 0;
}
function ke(e, t, n) {
  const i = y(e)?.title;
  if (typeof i == "string") return i;
  const a = y(i);
  if (!a) return "";
  if (typeof a.key == "string") return n(a.key);
  const l = a[t];
  if (typeof l == "string") return l;
  const o = Object.values(a).find((r) => typeof r == "string");
  return typeof o == "string" ? o : "";
}
function Ae(e, t) {
  const n = ve(e), i = y(n?.icon);
  if (!i) return n;
  for (const a of ["title", "lore"]) {
    const l = y(i[a]);
    l && typeof l.key == "string" && (i[a] = t(l.key));
  }
  return n;
}
function H(e) {
  const t = Number(e);
  return Number.isInteger(t) && t >= 1 ? t : 0;
}
function X(e) {
  const t = e.milestones;
  return Array.isArray(t) ? t.map((n) => y(n) ?? {}) : [];
}
function O(e) {
  const t = H(e.at), n = H(e.every);
  if (t > 0) return { at: t, every: 0, from: 0, icon: e.icon, actions: e.actions };
  if (n > 0) return { at: 0, every: n, from: H(e.from) || n, icon: e.icon, actions: e.actions };
}
function A(e) {
  return X(e).map(O).filter((t) => t !== void 0);
}
function _e(e, t) {
  return t < 1 ? !1 : e.every === 0 ? t === e.at : t >= e.from && (t - e.from) % e.every === 0;
}
function q(e, t) {
  const n = Math.max(t, 1);
  return e.every === 0 ? e.at >= n ? e.at : -1 : n <= e.from ? e.from : e.from + Math.ceil((n - e.from) / e.every) * e.every;
}
function Te(e, t) {
  return e.filter((n) => _e(n, t));
}
function U(e, t, n) {
  const i = [], a = e.map((l) => q(l, t));
  for (; i.length < n; ) {
    const l = a.filter((r) => r > 0);
    if (l.length === 0) break;
    const o = Math.min(...l);
    for (let r = 0; r < a.length && i.length < n; r++) {
      if (a[r] !== o) continue;
      const f = e[r];
      i.push({ streak: o, milestone: f }), a[r] = f.every > 0 ? q(f, o + 1) : -1;
    }
  }
  return i;
}
function je(e) {
  return e.length > 0 && !e.some((t) => t.every > 0);
}
function Q(e, t) {
  return e.every === 0 ? t("milestoneAt", { at: e.at }) : e.from === e.every ? t("milestoneEvery", { every: e.every }) : t("milestoneEveryFrom", { every: e.every, from: e.from });
}
const Se = "daily:calendar", K = "daily:status", Y = "daily:milestones", Ee = 28, De = 200, _ = 5, Re = "rewards", Le = 7, Ce = 20, B = 35, $ = { material: "PAPER" }, Z = 864e5;
function ee(e) {
  const t = e?.dataSources?.find(
    (n) => String(n.id) === Re
  );
  if (!(!t || t.shape !== "config"))
    return y(t.value);
}
function He(e) {
  if (ee(e)) return !0;
  const t = e;
  return t?.ownerModule === "daily" || String(t?.id ?? "") === "daily" || String(t?.id ?? "").endsWith(":daily");
}
function T(e, t) {
  return e < 1 ? 1 : (e - 1) % t + 1;
}
function R(e) {
  const t = y(e);
  if (!t || !("title" in t) || !te(t.title)) return e;
  const { title: n, ...i } = t;
  return i;
}
function te(e) {
  if (e == null) return !0;
  if (typeof e == "string") return e.trim() === "";
  const t = y(e);
  return t !== void 0 && Object.values(t).every(te);
}
function Me(e, t, n) {
  if (!t) return e;
  const i = (r, f) => {
    const m = y(r), u = y(m?.icon);
    if (!m || !u || y(u.title)) return r;
    const c = `${f}.icon.title`, h = {};
    typeof u.title == "string" && u.title.trim() !== "" && (h[n] = u.title);
    for (const [x, N] of Object.entries(t[c] ?? {}))
      !(x in h) && N.trim() !== "" && (h[x] = N);
    const { title: g, ...b } = u;
    return { ...m, icon: Object.keys(h).length > 0 ? { ...b, title: h } : b };
  }, a = { ...e }, l = "sources.rewards";
  e.default_day !== void 0 && (a.default_day = i(e.default_day, `${l}.default_day`));
  const o = y(e.days);
  return o && (a.days = Object.fromEntries(
    Object.entries(o).map(([r, f]) => [r, i(f, `${l}.days.${r.trim().toLowerCase()}`)])
  )), Array.isArray(e.milestones) && (a.milestones = e.milestones.map((r, f) => i(r, `${l}.milestones${f === 0 ? "" : `_${f + 1}`}`))), a;
}
function ne(e, t) {
  if (!(t < 1))
    return j(e).get(t) ?? y(e.default_day);
}
function S(e, t, n) {
  const i = e?.[t];
  if (i === void 0) return n;
  const a = Number.parseInt(String(i).trim(), 10);
  return Number.isNaN(a) ? n : a;
}
function Fe(e, t) {
  return t < 2 ? 0 : (e - 1) % t;
}
function Ie(e, t, n) {
  const i = [
    { status: "claimed", streak: 1 },
    { status: "missed", streak: 0 },
    { status: "claimed", streak: 1 },
    { status: "claimed", streak: 2 },
    { status: "claimed", streak: 3 },
    { status: "covered", streak: 0 },
    { status: "available", streak: 4 }
  ], a = [];
  for (let o = -e; o < 0; o++) {
    const r = i[i.length + o];
    a.push(
      r ? {
        offset: o,
        status: r.status,
        cycleDay: r.streak > 0 ? T(r.streak, n) : 0,
        streak: r.streak
      } : { offset: o, status: "before", cycleDay: 0, streak: 0 }
    );
  }
  const l = _;
  a.push({ offset: 0, status: "available", cycleDay: T(l, n), streak: l });
  for (let o = 1; o <= t; o++)
    a.push({ offset: o, status: "upcoming", cycleDay: T(l + o, n), streak: l + o });
  return a;
}
function k(e) {
  return String(e).padStart(2, "0");
}
function Oe(e, t, n) {
  const i = new Date(Date.UTC(n.getFullYear(), n.getMonth(), n.getDate()) + e.offset * Z), a = ne(t, e.cycleDay), l = a?.icon != null ? R(a.icon) : $;
  return {
    id: `${i.getUTCFullYear()}-${k(i.getUTCMonth() + 1)}-${k(i.getUTCDate())}`,
    date: `${k(i.getUTCDate())}/${k(i.getUTCMonth() + 1)}`,
    offset: e.offset,
    status: e.status,
    is_today: e.offset === 0,
    is_claimed: e.status === "claimed",
    is_missed: e.status === "missed",
    is_covered: e.status === "covered",
    is_before: e.status === "before",
    is_available: e.status === "available",
    is_upcoming: e.status === "upcoming",
    cycle_day: e.cycleDay,
    has_reward: a !== void 0,
    icon: l,
    // The sample player's streak never broke: each streak that a milestone matches pays it.
    ...ae(A(t), e.streak, e.streak > 0, l)
  };
}
function ae(e, t, n, i) {
  const a = n ? Te(e, t) : [];
  return {
    streak: t,
    is_milestone: a.length > 0,
    milestone_count: a.length,
    milestone_icon: R(a.find((l) => l.icon != null)?.icon) ?? i
  };
}
function Ue(e, t, n) {
  const i = A(e);
  return U(i, _, t).map(({ streak: a, milestone: l }) => {
    const o = a - _, r = new Date(Date.UTC(n.getFullYear(), n.getMonth(), n.getDate()) + o * Z);
    return {
      id: `${a}-${i.indexOf(l) + 1}`,
      streak: a,
      offset: o,
      date: `${k(r.getUTCDate())}/${k(r.getUTCMonth() + 1)}`,
      is_today: o === 0,
      repeats: l.every > 0,
      every: l.every,
      icon: l.icon != null ? R(l.icon) : $
    };
  });
}
function $e(e, t) {
  const n = ne(t, Number(e.cycle_day) || 0), i = n?.icon != null ? R(n.icon) : $, a = ae(A(t), Number(e.streak) || 0, e.is_milestone === !0, i);
  return { ...e, has_reward: n !== void 0, icon: i, ...a, is_milestone: e.is_milestone === !0 };
}
function ze(e, t = {}) {
  const n = U(A(t), _, 1)[0];
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
    cycle_day: T(5, e),
    cycle_length: e,
    until_reset: "3h 12m",
    has_milestones: A(t).length > 0,
    next_milestone: n ? n.streak : 0,
    next_milestone_in: n ? n.streak - _ : -1
  };
}
function Pe(e, t, n, i = /* @__PURE__ */ new Date()) {
  if (t !== Se && t !== K && t !== Y || !He(e)) return null;
  const a = e, l = Me(ee(e) ?? {}, a?.i18nKeys, a?.defaultLang ?? "en"), o = D(l), r = n?.serverRows;
  if (t === K) {
    const c = r?.filter((h) => h.loaded === !0) ?? [];
    return c.length > 0 ? c.map((h) => ({ ...h, cycle_length: o })) : [ze(o, l)];
  }
  if (t === Y) {
    if (r && r.length > 0) return r;
    const c = Math.max(0, Math.min(De, S(n?.params, "count", Ee)));
    return Ue(l, c, i);
  }
  if (r && r.length > 0) return r.map((c) => $e(c, l));
  const f = Math.max(0, Math.min(B, S(n?.params, "past", Le))), m = Math.max(0, S(n?.params, "future", Ce)), u = Math.min(Fe(T(_, o), S(n?.params, "week", 0)), m, B - f);
  return Ie(f + u, m - u, o).map((c) => Oe(c, l, i));
}
const qe = {
  cycleLength: "Cycle length",
  cycleHint: "Days before the rewards start again at day 1. The streak goes on.",
  days: "{{count}} days",
  legendTitle: "Legend",
  legendOwn: "Its own reward",
  legendDefault: "Reward of the other days",
  defaultTitle: "The other days",
  defaultHint: "What a day without its own reward pays.",
  defaultNone: "None: no reward on those days.",
  defaultAdd: "Add a reward for the other days",
  defaultRemove: "No reward on the other days",
  day: "Day {{day}}",
  ownHint: "Its own reward, in place of the other days' one.",
  ownStackHint: "Its own reward, paid on top of the other days' one.",
  stackLabel: "With the reward of the other days",
  stackFollow: "Default ({{mode}})",
  stackReplace: "Replaces it",
  stackAdd: "Adds to it",
  stackAllLabel: "A day with its own reward",
  stackAllHint: "Replaces this reward, or pays it too (this one first). Each day can say otherwise.",
  stackBadge: "Also pays the reward of the other days",
  legendStack: "Also pays the other days' reward",
  inheritHint: "This day pays the reward of the other days.",
  customize: "Give this day its own reward",
  editDefault: "Edit the reward of the other days",
  reset: "Back to the reward of the other days",
  icon: "Icon",
  iconHint: "Shown in the calendar menu: name it after the reward.",
  actions: "Actions",
  actionsHint: "Run when the day is claimed: give_item, give_money, a command… ({streak}, {cycle_day}, {cycle_length}).",
  steps: "{{count}} step(s)",
  noActions: "no action",
  outsideTitle: "Days past the cycle",
  outsideHint: "The plugin refuses the file while these days are listed: lengthen the cycle or remove them.",
  remove: "Remove day {{day}}",
  pickTitle: "Nothing selected",
  pick: 'Click a day, "The other days" or a milestone: its reward opens here.',
  milestonesTitle: "Streak milestones",
  milestonesHint: "Bonuses on top of the day's reward, when the streak reaches them. A break starts them again.",
  milestonesNone: "No milestone yet.",
  milestoneAddAt: "+ Once per streak",
  milestoneAddEvery: "+ Again and again",
  milestoneAt: "At {{at}} days",
  milestoneEvery: "Every {{every}} days",
  milestoneEveryFrom: "Every {{every}} days from {{from}}",
  milestoneInvalid: "Neither at nor every: the plugin refuses the file",
  milestoneNoRepeat: 'No milestone repeats: past the last one, a player earns more by breaking their streak than by going on. End with an "again and again" milestone.',
  milestoneKind: "Paid",
  milestoneOnce: "Once per streak",
  milestoneRepeat: "Again and again",
  milestoneAtLabel: "At streak",
  milestoneEveryLabel: "Every (days of streak)",
  milestoneFromLabel: "From streak",
  milestoneFromHint: "The first streak it pays. Empty: the same as every.",
  milestoneIconHint: "Shown in the milestones menu: name it after the bonus.",
  milestoneActionsHint: "Run with the claim of the day that reaches it ({streak}, {cycle_day}, {cycle_length}).",
  milestoneRemove: "Remove this milestone",
  milestoneNext: "Next ones from day 1: {{list}}"
}, Ke = {
  cycleLength: "Longueur du cycle",
  cycleHint: "Jours avant que les récompenses reprennent au jour 1. Le streak continue.",
  days: "{{count}} jours",
  legendTitle: "Légende",
  legendOwn: "Récompense propre",
  legendDefault: "Récompense des autres jours",
  defaultTitle: "Les autres jours",
  defaultHint: "Ce que rapporte un jour sans récompense propre.",
  defaultNone: "Aucune : pas de récompense ces jours-là.",
  defaultAdd: "Ajouter une récompense pour les autres jours",
  defaultRemove: "Pas de récompense les autres jours",
  day: "Jour {{day}}",
  ownHint: "Sa propre récompense, à la place de celle des autres jours.",
  ownStackHint: "Sa propre récompense, en plus de celle des autres jours.",
  stackLabel: "Avec la récompense des autres jours",
  stackFollow: "Par défaut ({{mode}})",
  stackReplace: "La remplace",
  stackAdd: "S'y ajoute",
  stackAllLabel: "Un jour avec sa propre récompense",
  stackAllHint: "Remplace cette récompense, ou la donne aussi (celle-ci d'abord). Chaque jour peut faire autrement.",
  stackBadge: "Donne aussi la récompense des autres jours",
  legendStack: "Paie aussi la récompense des autres jours",
  inheritHint: "Ce jour rapporte la récompense des autres jours.",
  customize: "Donner à ce jour sa propre récompense",
  editDefault: "Modifier la récompense des autres jours",
  reset: "Revenir à la récompense des autres jours",
  icon: "Icône",
  iconHint: "Affichée dans le calendrier : nomme-la d'après la récompense.",
  actions: "Actions",
  actionsHint: "Exécutées quand le jour est réclamé : give_item, give_money, une commande… ({streak}, {cycle_day}, {cycle_length}).",
  steps: "{{count}} étape(s)",
  noActions: "aucune action",
  outsideTitle: "Jours hors du cycle",
  outsideHint: "Le plugin refuse le fichier tant que ces jours y sont : allonge le cycle ou retire-les.",
  remove: "Retirer le jour {{day}}",
  pickTitle: "Aucune sélection",
  pick: "Clique sur un jour, « Les autres jours » ou un palier : sa récompense s'ouvre ici.",
  milestonesTitle: "Paliers de streak",
  milestonesHint: "Des bonus en plus de la récompense du jour, quand le streak les atteint. Un streak cassé les fait recommencer.",
  milestonesNone: "Aucun palier pour l'instant.",
  milestoneAddAt: "+ Une fois par streak",
  milestoneAddEvery: "+ Répétable",
  milestoneAt: "À {{at}} jours",
  milestoneEvery: "Tous les {{every}} jours",
  milestoneEveryFrom: "Tous les {{every}} jours dès {{from}}",
  milestoneInvalid: "Ni at ni every : le plugin refuse le fichier",
  milestoneNoRepeat: "Aucun palier ne se répète : passé le dernier, un joueur gagne plus à casser son streak qu'à continuer. Termine par un palier répétable.",
  milestoneKind: "Payé",
  milestoneOnce: "Une fois par streak",
  milestoneRepeat: "Répétable",
  milestoneAtLabel: "Au streak",
  milestoneEveryLabel: "Tous les (jours de streak)",
  milestoneFromLabel: "À partir du streak",
  milestoneFromHint: "Le premier streak qui le paie. Vide : le même que every.",
  milestoneIconHint: "Affichée dans le menu des paliers : nomme-la d'après le bonus.",
  milestoneActionsHint: "Exécutées avec la réclamation du jour qui l'atteint ({streak}, {cycle_day}, {cycle_length}).",
  milestoneRemove: "Supprimer ce palier",
  milestoneNext: "Les prochains depuis le jour 1 : {{list}}"
}, w = se({ en: qe, fr: Ke });
function Ye({ projectId: e, value: t, set: n, readOnly: i }) {
  const a = w(), l = y(t) ?? {}, o = he(l), r = ge(l), f = y(l.default_day), [m, u] = me(null), c = W(e), h = (p) => c ? J(c, p, ce(c)[0] ?? "en") : "", g = (p, v) => n(["days"], be(l, p, v)), b = X(l), x = (p) => n(["milestones"], p.length > 0 ? p : void 0), N = (p) => {
    x([...b, p]), u({ milestone: b.length });
  }, C = typeof m == "object" && m !== null ? m.milestone : -1;
  return /* @__PURE__ */ d("div", { className: "daily-rewards", children: [
    /* @__PURE__ */ d("div", { className: "daily-toolbar", children: [
      /* @__PURE__ */ d("label", { className: "daily-cycle", children: [
        /* @__PURE__ */ s("span", { className: "field-label", children: a("cycleLength") }),
        /* @__PURE__ */ s(
          "input",
          {
            className: "input daily-cycle-input",
            type: "number",
            min: 1,
            value: D(l),
            disabled: i,
            onChange: (p) => {
              const v = Number.parseInt(p.target.value, 10);
              Number.isInteger(v) && v >= 1 && n(["cycle_length"], v);
            }
          }
        ),
        /* @__PURE__ */ s("span", { className: "daily-faint", children: a("cycleHint") })
      ] }),
      /* @__PURE__ */ d("div", { className: "daily-legend", role: "note", "aria-label": a("legendTitle"), children: [
        /* @__PURE__ */ s("span", { className: "daily-legend-title", children: a("legendTitle") }),
        /* @__PURE__ */ d("span", { className: "daily-legend-item", children: [
          /* @__PURE__ */ s("span", { className: "daily-legend-swatch is-own", "aria-hidden": "true" }),
          a("legendOwn")
        ] }),
        /* @__PURE__ */ d("span", { className: "daily-legend-item", children: [
          /* @__PURE__ */ s("span", { className: "daily-legend-swatch is-default", "aria-hidden": "true" }),
          a("legendDefault")
        ] }),
        o.some((p) => p.stacks) && /* @__PURE__ */ d("span", { className: "daily-legend-item", children: [
          /* @__PURE__ */ s("span", { className: "daily-legend-swatch is-own", "aria-hidden": "true", children: /* @__PURE__ */ s("span", { className: "daily-legend-plus", children: "+" }) }),
          a("legendStack")
        ] })
      ] })
    ] }),
    r.length > 0 && /* @__PURE__ */ d("div", { className: "daily-outside", role: "alert", children: [
      /* @__PURE__ */ s("strong", { children: a("outsideTitle") }),
      /* @__PURE__ */ s("span", { children: a("outsideHint") }),
      /* @__PURE__ */ s("div", { className: "daily-outside-days", children: r.map((p) => /* @__PURE__ */ d(
        "button",
        {
          type: "button",
          className: "chip",
          disabled: i,
          onClick: () => g(p, void 0),
          children: [
            a("remove", { day: p }),
            " ×"
          ]
        },
        p
      )) })
    ] }),
    /* @__PURE__ */ d("div", { className: "daily-layout", children: [
      /* @__PURE__ */ d("div", { className: "daily-board", children: [
        /* @__PURE__ */ d(
          "button",
          {
            type: "button",
            className: `daily-default${m === "default" ? " is-selected" : ""}`,
            onClick: () => u("default"),
            children: [
              /* @__PURE__ */ s(L, { reward: f }),
              /* @__PURE__ */ d("span", { className: "daily-default-text", children: [
                /* @__PURE__ */ s("span", { className: "daily-default-title", children: a("defaultTitle") }),
                /* @__PURE__ */ s("span", { className: "daily-faint", children: a(f ? "defaultHint" : "defaultNone") })
              ] })
            ]
          }
        ),
        /* @__PURE__ */ s("div", { className: "daily-grid", style: { gridTemplateColumns: `repeat(${fe}, minmax(0, 1fr))` }, children: o.map((p) => /* @__PURE__ */ s(
          We,
          {
            cell: p,
            projectId: e,
            selected: m === p.day,
            onSelect: () => u(p.day)
          },
          p.day
        )) }),
        /* @__PURE__ */ s(
          Ve,
          {
            rewards: l,
            entries: b,
            projectId: e,
            selected: C,
            readOnly: i,
            onSelect: (p) => u({ milestone: p }),
            onAdd: N
          }
        )
      ] }),
      /* @__PURE__ */ s("aside", { className: `daily-panel${m === null ? " is-empty" : ""}`, children: m === null ? /* @__PURE__ */ d("div", { className: "daily-panel-empty", children: [
        /* @__PURE__ */ s("span", { className: "daily-panel-empty-title", children: a("pickTitle") }),
        /* @__PURE__ */ s("span", { className: "daily-faint", children: a("pick") })
      ] }) : typeof m == "object" ? b[m.milestone] ? /* @__PURE__ */ s(
        Xe,
        {
          projectId: e,
          entry: b[m.milestone],
          readOnly: i,
          onChange: (p) => x(b.map((v, ie) => ie === m.milestone ? p : v)),
          onRemove: () => {
            x(b.filter((p, v) => v !== m.milestone)), u(null);
          }
        }
      ) : /* @__PURE__ */ s("p", { className: "daily-faint", children: a("pick") }) : m === "default" ? /* @__PURE__ */ s(E, { title: a("defaultTitle"), hint: a(f ? "defaultHint" : "defaultNone"), children: f ? /* @__PURE__ */ d(V, { children: [
        /* @__PURE__ */ s(
          P,
          {
            projectId: e,
            reward: f,
            readOnly: i,
            onChange: (p, v) => n(["default_day", ...p], v)
          }
        ),
        /* @__PURE__ */ s("div", { className: "daily-fields", children: /* @__PURE__ */ d("div", { children: [
          /* @__PURE__ */ s("div", { className: "field-label", children: a("stackAllLabel") }),
          /* @__PURE__ */ s("div", { className: "daily-faint", children: a("stackAllHint") }),
          /* @__PURE__ */ s("div", { className: "daily-milestone-kind", children: [!1, !0].map((p) => /* @__PURE__ */ s(
            "button",
            {
              type: "button",
              className: `chip${F(l) === p ? " is-active" : ""}`,
              disabled: i,
              onClick: () => n(["with_default_day"], p),
              children: a(p ? "stackAdd" : "stackReplace")
            },
            String(p)
          )) })
        ] }) }),
        /* @__PURE__ */ s(
          "button",
          {
            type: "button",
            className: "btn btn-ghost btn-sm",
            disabled: i,
            onClick: () => n(["default_day"], void 0),
            children: a("defaultRemove")
          }
        )
      ] }) : /* @__PURE__ */ s(
        "button",
        {
          type: "button",
          className: "btn btn-sm",
          disabled: i,
          onClick: () => n(["default_day"], { icon: { material: "BREAD" }, actions: [] }),
          children: a("defaultAdd")
        }
      ) }) : /* @__PURE__ */ s(
        Je,
        {
          day: m,
          projectId: e,
          own: j(l).get(m),
          fallback: f,
          stacksAll: F(l),
          stacks: G(l, j(l).get(m) ?? {}),
          readOnly: i,
          onCustomize: () => g(m, Ae(f, h) ?? { icon: { material: "CHEST" }, actions: [] }),
          onReset: () => g(m, void 0),
          onChange: (p, v) => n(["days", Be(l, m), ...p], v),
          onEditDefault: () => u("default")
        }
      ) })
    ] })
  ] });
}
function Be(e, t) {
  return Object.keys(y(e.days) ?? {}).find((n) => Number(n) === t) ?? String(t);
}
function We({
  cell: e,
  projectId: t,
  selected: n,
  onSelect: i
}) {
  const a = w(), l = z(t, e.reward?.icon), o = ["daily-cell", e.own ? "is-own" : "is-default", n ? "is-selected" : ""];
  return /* @__PURE__ */ d("button", { type: "button", className: o.join(" "), onClick: i, title: a("day", { day: e.day }), children: [
    /* @__PURE__ */ s("span", { className: "daily-cell-day", children: e.day }),
    e.stacks && /* @__PURE__ */ s("span", { className: "daily-cell-stack", title: a("stackBadge"), "aria-label": a("stackBadge"), children: "+" }),
    /* @__PURE__ */ s(L, { reward: e.reward }),
    e.own && l && /* @__PURE__ */ s("span", { className: "daily-cell-title", children: /* @__PURE__ */ s(I, { source: l, maxLines: 2, align: "center", scale: 1 }) })
  ] });
}
function L({ reward: e }) {
  const t = e?.icon, n = xe(t), i = le(n), a = we(t);
  return /* @__PURE__ */ d("span", { className: "daily-slot", children: [
    i ? /* @__PURE__ */ s("img", { src: i, alt: n, draggable: !1 }) : /* @__PURE__ */ s("span", { className: "daily-slot-name", children: n ? n.slice(0, 3) : "—" }),
    a > 1 && /* @__PURE__ */ s("span", { className: "daily-slot-count", children: a })
  ] });
}
function z(e, t) {
  const n = W(e), i = de();
  return ke(t, i, (a) => n ? J(n, a, i) : "");
}
function E({ title: e, hint: t, children: n }) {
  return /* @__PURE__ */ d("div", { className: "daily-panel-body", children: [
    /* @__PURE__ */ d("div", { children: [
      /* @__PURE__ */ s("div", { className: "daily-panel-title", children: e }),
      /* @__PURE__ */ s("div", { className: "daily-faint", children: t })
    ] }),
    n
  ] });
}
function Je({
  day: e,
  projectId: t,
  own: n,
  fallback: i,
  stacksAll: a,
  stacks: l,
  readOnly: o,
  onCustomize: r,
  onReset: f,
  onChange: m,
  onEditDefault: u
}) {
  const c = w(), h = z(t, (n ?? i)?.icon);
  if (!n) {
    const g = Ne(i);
    return /* @__PURE__ */ d(E, { title: c("day", { day: e }), hint: c("inheritHint"), children: [
      /* @__PURE__ */ d("div", { className: "daily-inherited", children: [
        /* @__PURE__ */ s(L, { reward: i }),
        /* @__PURE__ */ d("span", { className: "daily-inherited-text", children: [
          h ? /* @__PURE__ */ s(I, { source: h, scale: 1 }) : /* @__PURE__ */ s("span", { className: "daily-faint", children: c("defaultNone") }),
          /* @__PURE__ */ s("span", { className: "daily-faint", children: g > 0 ? c("steps", { count: g }) : c("noActions") })
        ] })
      ] }),
      /* @__PURE__ */ s("button", { type: "button", className: "btn btn-sm", disabled: o, onClick: r, children: c("customize") }),
      /* @__PURE__ */ s("button", { type: "button", className: "btn btn-ghost btn-sm", onClick: u, children: c("editDefault") })
    ] });
  }
  return /* @__PURE__ */ d(E, { title: c("day", { day: e }), hint: c(i && l ? "ownStackHint" : "ownHint"), children: [
    /* @__PURE__ */ s(P, { projectId: t, reward: n, readOnly: o, onChange: m }),
    i && /* @__PURE__ */ s("fieldset", { className: "daily-fields", disabled: o, children: /* @__PURE__ */ d("div", { children: [
      /* @__PURE__ */ s("div", { className: "field-label", children: c("stackLabel") }),
      /* @__PURE__ */ s("div", { className: "daily-milestone-kind", children: [void 0, !1, !0].map((g) => /* @__PURE__ */ s(
        "button",
        {
          type: "button",
          className: `chip${n.with_default_day === g ? " is-active" : ""}`,
          onClick: () => m(["with_default_day"], g),
          children: g === void 0 ? c("stackFollow", { mode: c(a ? "stackAdd" : "stackReplace") }) : c(g ? "stackAdd" : "stackReplace")
        },
        String(g)
      )) })
    ] }) }),
    /* @__PURE__ */ s("button", { type: "button", className: "btn btn-ghost btn-sm", disabled: o, onClick: f, children: c("reset") })
  ] });
}
function P({
  projectId: e,
  reward: t,
  readOnly: n,
  iconHint: i,
  actionsHint: a,
  onChange: l
}) {
  const o = w();
  return /* @__PURE__ */ d("fieldset", { className: "daily-fields", disabled: n, children: [
    /* @__PURE__ */ d("div", { children: [
      /* @__PURE__ */ s("div", { className: "field-label", children: o("icon") }),
      /* @__PURE__ */ s("div", { className: "daily-faint", children: i ?? o("iconHint") }),
      /* @__PURE__ */ s(re, { projectId: e, value: t.icon, onChange: (r) => l(["icon"], r) })
    ] }),
    /* @__PURE__ */ d("div", { children: [
      /* @__PURE__ */ s("div", { className: "field-label", children: o("actions") }),
      /* @__PURE__ */ s("div", { className: "daily-faint", children: a ?? o("actionsHint") }),
      /* @__PURE__ */ s(
        oe,
        {
          projectId: e,
          value: Array.isArray(t.actions) ? t.actions : [],
          onChange: (r) => l(["actions"], r)
        }
      )
    ] })
  ] });
}
function Ve({
  rewards: e,
  entries: t,
  projectId: n,
  selected: i,
  readOnly: a,
  onSelect: l,
  onAdd: o
}) {
  const r = w(), f = A(e), m = U(f, 1, 8).map((u) => u.streak);
  return /* @__PURE__ */ d("section", { className: "daily-milestones", children: [
    /* @__PURE__ */ d("div", { children: [
      /* @__PURE__ */ s("div", { className: "daily-panel-title", children: r("milestonesTitle") }),
      /* @__PURE__ */ s("div", { className: "daily-faint", children: r("milestonesHint") })
    ] }),
    je(f) && /* @__PURE__ */ s("div", { className: "daily-outside", role: "alert", children: r("milestoneNoRepeat") }),
    t.length === 0 ? /* @__PURE__ */ s("span", { className: "daily-faint", children: r("milestonesNone") }) : /* @__PURE__ */ s("div", { className: "daily-milestone-list", children: t.map((u, c) => /* @__PURE__ */ s(
      Ge,
      {
        entry: u,
        projectId: n,
        selected: i === c,
        onSelect: () => l(c)
      },
      c
    )) }),
    m.length > 0 && /* @__PURE__ */ s("span", { className: "daily-faint", children: r("milestoneNext", { list: m.join(", ") }) }),
    /* @__PURE__ */ d("div", { className: "daily-milestone-add", children: [
      /* @__PURE__ */ s(
        "button",
        {
          type: "button",
          className: "btn btn-sm",
          disabled: a,
          onClick: () => o({ at: 30, icon: { material: "NETHER_STAR" }, actions: [] }),
          children: r("milestoneAddAt")
        }
      ),
      /* @__PURE__ */ s(
        "button",
        {
          type: "button",
          className: "btn btn-sm",
          disabled: a,
          onClick: () => o({ every: 50, icon: { material: "NETHER_STAR" }, actions: [] }),
          children: r("milestoneAddEvery")
        }
      )
    ] })
  ] });
}
function Ge({
  entry: e,
  projectId: t,
  selected: n,
  onSelect: i
}) {
  const a = w(), l = z(t, e.icon), o = O(e);
  return /* @__PURE__ */ d("button", { type: "button", className: ["daily-default", "daily-milestone", n ? "is-selected" : "", o ? "" : "is-invalid"].join(" "), onClick: i, children: [
    /* @__PURE__ */ s(L, { reward: e }),
    /* @__PURE__ */ d("span", { className: "daily-default-text", children: [
      /* @__PURE__ */ s("span", { className: "daily-default-title", children: o ? Q(o, a) : a("milestoneInvalid") }),
      l && /* @__PURE__ */ s(I, { source: l, maxLines: 1, scale: 1 })
    ] })
  ] });
}
function M(e) {
  const t = Number.parseInt(e, 10);
  return Number.isInteger(t) && t >= 1 ? t : void 0;
}
function Xe({
  projectId: e,
  entry: t,
  readOnly: n,
  onChange: i,
  onRemove: a
}) {
  const l = w(), o = O(t), r = t.at === void 0 && t.every !== void 0, f = (u, c) => {
    const h = { ...t, [u]: c };
    c === void 0 && delete h[u];
    const { at: g, every: b, from: x, ...N } = h;
    return Object.fromEntries(
      Object.entries({ at: g, every: b, from: x, ...N }).filter(([, C]) => C !== void 0)
    );
  }, m = (u) => {
    if (u === r) return;
    const { at: c, every: h, from: g, ...b } = t, x = Number(c) || Number(h) || 30;
    i(u ? { every: x, ...b } : { at: x, ...b });
  };
  return /* @__PURE__ */ d(E, { title: o ? Q(o, l) : l("milestoneInvalid"), hint: l("milestonesHint"), children: [
    /* @__PURE__ */ d("fieldset", { className: "daily-fields", disabled: n, children: [
      /* @__PURE__ */ d("div", { children: [
        /* @__PURE__ */ s("div", { className: "field-label", children: l("milestoneKind") }),
        /* @__PURE__ */ d("div", { className: "daily-milestone-kind", children: [
          /* @__PURE__ */ s("button", { type: "button", className: `chip${r ? "" : " is-active"}`, onClick: () => m(!1), children: l("milestoneOnce") }),
          /* @__PURE__ */ s("button", { type: "button", className: `chip${r ? " is-active" : ""}`, onClick: () => m(!0), children: l("milestoneRepeat") })
        ] })
      ] }),
      r ? /* @__PURE__ */ d(V, { children: [
        /* @__PURE__ */ d("label", { children: [
          /* @__PURE__ */ s("span", { className: "field-label", children: l("milestoneEveryLabel") }),
          /* @__PURE__ */ s(
            "input",
            {
              className: "input",
              type: "number",
              min: 1,
              value: String(t.every ?? ""),
              onChange: (u) => i(f("every", M(u.target.value)))
            }
          )
        ] }),
        /* @__PURE__ */ d("label", { children: [
          /* @__PURE__ */ s("span", { className: "field-label", children: l("milestoneFromLabel") }),
          /* @__PURE__ */ s("span", { className: "daily-faint", children: l("milestoneFromHint") }),
          /* @__PURE__ */ s(
            "input",
            {
              className: "input",
              type: "number",
              min: 1,
              value: String(t.from ?? ""),
              placeholder: String(t.every ?? ""),
              onChange: (u) => i(f("from", M(u.target.value)))
            }
          )
        ] })
      ] }) : /* @__PURE__ */ d("label", { children: [
        /* @__PURE__ */ s("span", { className: "field-label", children: l("milestoneAtLabel") }),
        /* @__PURE__ */ s(
          "input",
          {
            className: "input",
            type: "number",
            min: 1,
            value: String(t.at ?? ""),
            onChange: (u) => i(f("at", M(u.target.value)))
          }
        )
      ] })
    ] }),
    /* @__PURE__ */ s(
      P,
      {
        projectId: e,
        reward: t,
        readOnly: n,
        iconHint: l("milestoneIconHint"),
        actionsHint: l("milestoneActionsHint"),
        onChange: (u, c) => i(f(u[0], c))
      }
    ),
    /* @__PURE__ */ s("button", { type: "button", className: "btn btn-ghost btn-sm", disabled: n, onClick: a, children: l("milestoneRemove") })
  ] });
}
const Qe = `
.daily-rewards { display: flex; flex-direction: column; gap: 16px; }
.daily-toolbar { display: flex; flex-wrap: wrap; align-items: flex-end; justify-content: space-between; gap: 12px; }
.daily-cycle { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; }
.daily-cycle-input { width: 80px; }
.daily-faint { color: var(--text-faint); font-size: 12px; line-height: 1.4; }
.daily-legend { display: flex; flex-wrap: wrap; align-items: center; gap: 6px 14px; padding: 6px 10px;
  border-radius: var(--r-md); border: 1px dashed var(--border-soft); font-size: 12px; color: var(--text-faint);
  cursor: default; user-select: none; }
.daily-legend-title { font-size: 10px; font-weight: 600; letter-spacing: 0.08em; text-transform: uppercase;
  color: var(--text-faint); }
.daily-legend-item { display: inline-flex; align-items: center; gap: 6px; }
.daily-legend-swatch { position: relative; flex: none; display: inline-flex; align-items: center; justify-content: center;
  width: 18px; height: 22px; border-radius: 4px; border: 1px solid var(--border-soft); background: var(--surface-inset); }
.daily-legend-swatch::after { content: ""; width: 8px; height: 8px; margin-top: 4px; border-radius: 1px; background: var(--mc-slot-bg); }
.daily-legend-swatch.is-default::after { opacity: 0.45; }
.daily-legend-swatch.is-own { border: 2px solid var(--fam-reward); background: var(--fam-reward-soft); }
.daily-legend-plus { position: absolute; top: -1px; right: 1px; font-size: 9px; font-weight: 700; line-height: 1;
  color: var(--fam-reward); }
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
.daily-cell-stack { position: absolute; top: 3px; right: 6px; font-size: 12px; font-weight: 700; line-height: 1;
  color: var(--fam-reward); }
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
.daily-panel.is-empty { display: flex; align-items: center; justify-content: center; min-height: 320px;
  border-style: dashed; background: transparent; }
.daily-panel-empty { display: flex; flex-direction: column; gap: 6px; max-width: 260px; text-align: center; }
.daily-panel-empty-title { font-weight: 600; font-size: 13px; color: var(--text-mut); }
.daily-panel-body { display: flex; flex-direction: column; gap: 14px; align-items: stretch; }
.daily-panel-title { font-weight: 600; font-size: 15px; }
.daily-fields { display: flex; flex-direction: column; gap: 14px; border: 0; padding: 0; margin: 0; min-width: 0; }
.daily-fields > div { display: flex; flex-direction: column; gap: 6px; }
.daily-inherited { display: flex; align-items: center; gap: 10px; padding: 10px; border-radius: var(--r-md);
  background: var(--surface-inset); }
.daily-inherited-text { display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.daily-milestones { display: flex; flex-direction: column; gap: 10px; padding-top: 12px; border-top: 1px solid var(--border-soft); }
.daily-milestone-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 8px; }
.daily-milestone { border-style: solid; }
.daily-milestone.is-invalid { border-color: var(--warning); }
.daily-milestone-add { display: flex; flex-wrap: wrap; gap: 8px; }
.daily-milestone-kind { display: flex; flex-wrap: wrap; gap: 6px; }
.daily-fields label { display: flex; flex-direction: column; gap: 6px; }
`;
ue("daily", Qe);
const Ze = {
  id: "daily",
  displayName: "Daily rewards",
  schemaVersion: "daily/v1",
  sidebar: { icon: () => null, label: "Daily rewards", to: "/m/daily" },
  routes: [],
  screens: { rewards: Ye },
  // The calendar menu previewed in the editor: its days, from the rewards draft.
  localPreviewRows: Pe
}, at = { ...Ze, sdk: "1.20.0" };
export {
  at as default
};
