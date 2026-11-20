import { createModuleI18n as se, useProject as W, useItemTexture as le, McText as I, ItemValueInput as re, ActionChainEditor as oe, useContentLang as de, textValue as J, projectLangs as ce, injectModuleStyles as ue } from "@artisan/editor-sdk";
import { jsxs as c, jsx as l, Fragment as V } from "react/jsx-runtime";
import { useState as me } from "react";
const fe = 28, pe = 7, ye = "with_default_day";
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
  return Number.isInteger(t) && t >= 1 ? t : fe;
}
function S(e) {
  const t = /* @__PURE__ */ new Map();
  for (const [n, i] of Object.entries(y(e.days) ?? {})) {
    const a = Number(n);
    Number.isInteger(a) && a >= 1 && t.set(a, y(i) ?? {});
  }
  return t;
}
function he(e) {
  const t = D(e), n = S(e), i = y(e.default_day);
  return Array.from({ length: t }, (a, s) => {
    const o = s + 1, r = n.get(o), p = r !== void 0 && i !== void 0 && G(e, r);
    return { day: o, own: r !== void 0, reward: r ?? i, stacks: p };
  });
}
function ge(e) {
  const t = D(e);
  return [...S(e).keys()].filter((n) => n > t).sort((n, i) => n - i);
}
function be(e, t, n) {
  const i = Object.entries(y(e.days) ?? {}).filter(([a]) => Number(a) !== t);
  return n !== void 0 && i.push([String(t), n]), i.sort(([a], [s]) => Number(a) - Number(s)), Object.fromEntries(i);
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
  const s = a[t];
  if (typeof s == "string") return s;
  const o = Object.values(a).find((r) => typeof r == "string");
  return typeof o == "string" ? o : "";
}
function Ae(e, t) {
  const n = ve(e), i = y(n?.icon);
  if (!i) return n;
  for (const a of ["title", "lore"]) {
    const s = y(i[a]);
    s && typeof s.key == "string" && (i[a] = t(s.key));
  }
  return n;
}
function L(e) {
  const t = Number(e);
  return Number.isInteger(t) && t >= 1 ? t : 0;
}
function X(e) {
  const t = e.milestones;
  return Array.isArray(t) ? t.map((n) => y(n) ?? {}) : [];
}
function O(e) {
  const t = L(e.at), n = L(e.every);
  if (t > 0) return { at: t, every: 0, from: 0, icon: e.icon, actions: e.actions };
  if (n > 0) return { at: 0, every: n, from: L(e.from) || n, icon: e.icon, actions: e.actions };
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
  const i = [], a = e.map((s) => q(s, t));
  for (; i.length < n; ) {
    const s = a.filter((r) => r > 0);
    if (s.length === 0) break;
    const o = Math.min(...s);
    for (let r = 0; r < a.length && i.length < n; r++) {
      if (a[r] !== o) continue;
      const p = e[r];
      i.push({ streak: o, milestone: p }), a[r] = p.every > 0 ? q(p, o + 1) : -1;
    }
  }
  return i;
}
function Se(e) {
  return e.length > 0 && !e.some((t) => t.every > 0);
}
function Q(e, t) {
  return e.every === 0 ? t("milestoneAt", { at: e.at }) : e.from === e.every ? t("milestoneEvery", { every: e.every }) : t("milestoneEveryFrom", { every: e.every, from: e.from });
}
const Ee = "daily:calendar", K = "daily:status", Y = "daily:milestones", je = 28, De = 200, _ = 5, Re = "rewards", Ce = 7, He = 20, B = 35, $ = { material: "PAPER" }, Z = 864e5;
function ee(e) {
  const t = e?.dataSources?.find(
    (n) => String(n.id) === Re
  );
  if (!(!t || t.shape !== "config"))
    return y(t.value);
}
function Le(e) {
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
  const i = (r, p) => {
    const f = y(r), u = y(f?.icon);
    if (!f || !u || y(u.title)) return r;
    const d = `${p}.icon.title`, h = {};
    typeof u.title == "string" && u.title.trim() !== "" && (h[n] = u.title);
    for (const [x, N] of Object.entries(t[d] ?? {}))
      !(x in h) && N.trim() !== "" && (h[x] = N);
    const { title: g, ...b } = u;
    return { ...f, icon: Object.keys(h).length > 0 ? { ...b, title: h } : b };
  }, a = { ...e }, s = "sources.rewards";
  e.default_day !== void 0 && (a.default_day = i(e.default_day, `${s}.default_day`));
  const o = y(e.days);
  return o && (a.days = Object.fromEntries(
    Object.entries(o).map(([r, p]) => [r, i(p, `${s}.days.${r.trim().toLowerCase()}`)])
  )), Array.isArray(e.milestones) && (a.milestones = e.milestones.map((r, p) => i(r, `${s}.milestones${p === 0 ? "" : `_${p + 1}`}`))), a;
}
function ne(e, t) {
  if (!(t < 1))
    return S(e).get(t) ?? y(e.default_day);
}
function E(e, t, n) {
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
  const s = _;
  a.push({ offset: 0, status: "available", cycleDay: T(s, n), streak: s });
  for (let o = 1; o <= t; o++)
    a.push({ offset: o, status: "upcoming", cycleDay: T(s + o, n), streak: s + o });
  return a;
}
function k(e) {
  return String(e).padStart(2, "0");
}
function Oe(e, t, n) {
  const i = new Date(Date.UTC(n.getFullYear(), n.getMonth(), n.getDate()) + e.offset * Z), a = ne(t, e.cycleDay), s = a?.icon != null ? R(a.icon) : $;
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
    icon: s,
    // The sample player's streak never broke: each streak that a milestone matches pays it.
    ...ae(A(t), e.streak, e.streak > 0, s)
  };
}
function ae(e, t, n, i) {
  const a = n ? Te(e, t) : [];
  return {
    streak: t,
    is_milestone: a.length > 0,
    milestone_count: a.length,
    milestone_icon: R(a.find((s) => s.icon != null)?.icon) ?? i
  };
}
function Ue(e, t, n) {
  const i = A(e);
  return U(i, _, t).map(({ streak: a, milestone: s }) => {
    const o = a - _, r = new Date(Date.UTC(n.getFullYear(), n.getMonth(), n.getDate()) + o * Z);
    return {
      id: `${a}-${i.indexOf(s) + 1}`,
      streak: a,
      offset: o,
      date: `${k(r.getUTCDate())}/${k(r.getUTCMonth() + 1)}`,
      is_today: o === 0,
      repeats: s.every > 0,
      every: s.every,
      icon: s.icon != null ? R(s.icon) : $
    };
  });
}
function $e(e, t) {
  const n = ne(t, Number(e.cycle_day) || 0), i = n?.icon != null ? R(n.icon) : $, a = ae(A(t), Number(e.streak) || 0, e.is_milestone === !0, i);
  return { ...e, has_reward: n !== void 0, icon: i, ...a, is_milestone: e.is_milestone === !0 };
}
function Pe(e, t = {}) {
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
function ze(e, t, n, i = /* @__PURE__ */ new Date()) {
  if (t !== Ee && t !== K && t !== Y || !Le(e)) return null;
  const a = e, s = Me(ee(e) ?? {}, a?.i18nKeys, a?.defaultLang ?? "en"), o = D(s), r = n?.serverRows;
  if (t === K) {
    const d = r?.filter((h) => h.loaded === !0) ?? [];
    return d.length > 0 ? d.map((h) => ({ ...h, cycle_length: o })) : [Pe(o, s)];
  }
  if (t === Y) {
    if (r && r.length > 0) return r;
    const d = Math.max(0, Math.min(De, E(n?.params, "count", je)));
    return Ue(s, d, i);
  }
  if (r && r.length > 0) return r.map((d) => $e(d, s));
  const p = Math.max(0, Math.min(B, E(n?.params, "past", Ce))), f = Math.max(0, E(n?.params, "future", He)), u = Math.min(Fe(T(_, o), E(n?.params, "week", 0)), f, B - p);
  return Ie(p + u, f - u, o).map((d) => Oe(d, s, i));
}
const qe = {
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
  ownStackHint: "Its own reward, paid on top of the other days' one.",
  stackLabel: "With the reward of the other days",
  stackFollow: "Default ({{mode}})",
  stackReplace: "Replaces it",
  stackAdd: "Adds to it",
  stackAllLabel: "A day with its own reward",
  stackAllHint: "Replaces this reward, or pays it too (this one first). Each day can say otherwise.",
  stackBadge: "Also pays the reward of the other days",
  legendStack: "+ also the other days' reward",
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
  pick: "Click a day to edit its reward.",
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
  legendStack: "+ aussi la récompense des autres jours",
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
  pick: "Clique sur un jour pour modifier sa récompense.",
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
  const a = w(), s = y(t) ?? {}, o = he(s), r = ge(s), p = y(s.default_day), [f, u] = me(null), d = W(e), h = (m) => d ? J(d, m, ce(d)[0] ?? "en") : "", g = (m, v) => n(["days"], be(s, m, v)), b = X(s), x = (m) => n(["milestones"], m.length > 0 ? m : void 0), N = (m) => {
    x([...b, m]), u({ milestone: b.length });
  }, H = typeof f == "object" && f !== null ? f.milestone : -1;
  return /* @__PURE__ */ c("div", { className: "daily-rewards", children: [
    /* @__PURE__ */ c("div", { className: "daily-toolbar", children: [
      /* @__PURE__ */ c("label", { className: "daily-cycle", children: [
        /* @__PURE__ */ l("span", { className: "field-label", children: a("cycleLength") }),
        /* @__PURE__ */ l(
          "input",
          {
            className: "input daily-cycle-input",
            type: "number",
            min: 1,
            value: D(s),
            disabled: i,
            onChange: (m) => {
              const v = Number.parseInt(m.target.value, 10);
              Number.isInteger(v) && v >= 1 && n(["cycle_length"], v);
            }
          }
        ),
        /* @__PURE__ */ l("span", { className: "daily-faint", children: a("cycleHint") })
      ] }),
      /* @__PURE__ */ c("div", { className: "daily-legend", children: [
        /* @__PURE__ */ l("span", { className: "daily-legend-own", children: a("legendOwn") }),
        /* @__PURE__ */ l("span", { className: "daily-legend-default", children: a("legendDefault") }),
        o.some((m) => m.stacks) && /* @__PURE__ */ l("span", { className: "daily-legend-stack", children: a("legendStack") })
      ] })
    ] }),
    r.length > 0 && /* @__PURE__ */ c("div", { className: "daily-outside", role: "alert", children: [
      /* @__PURE__ */ l("strong", { children: a("outsideTitle") }),
      /* @__PURE__ */ l("span", { children: a("outsideHint") }),
      /* @__PURE__ */ l("div", { className: "daily-outside-days", children: r.map((m) => /* @__PURE__ */ c(
        "button",
        {
          type: "button",
          className: "chip",
          disabled: i,
          onClick: () => g(m, void 0),
          children: [
            a("remove", { day: m }),
            " ×"
          ]
        },
        m
      )) })
    ] }),
    /* @__PURE__ */ c("div", { className: "daily-layout", children: [
      /* @__PURE__ */ c("div", { className: "daily-board", children: [
        /* @__PURE__ */ c(
          "button",
          {
            type: "button",
            className: `daily-default${f === "default" ? " is-selected" : ""}`,
            onClick: () => u("default"),
            children: [
              /* @__PURE__ */ l(C, { reward: p }),
              /* @__PURE__ */ c("span", { className: "daily-default-text", children: [
                /* @__PURE__ */ l("span", { className: "daily-default-title", children: a("defaultTitle") }),
                /* @__PURE__ */ l("span", { className: "daily-faint", children: a(p ? "defaultHint" : "defaultNone") })
              ] })
            ]
          }
        ),
        /* @__PURE__ */ l("div", { className: "daily-grid", style: { gridTemplateColumns: `repeat(${pe}, minmax(0, 1fr))` }, children: o.map((m) => /* @__PURE__ */ l(
          We,
          {
            cell: m,
            projectId: e,
            selected: f === m.day,
            onSelect: () => u(m.day)
          },
          m.day
        )) }),
        /* @__PURE__ */ l(
          Ve,
          {
            rewards: s,
            entries: b,
            projectId: e,
            selected: H,
            readOnly: i,
            onSelect: (m) => u({ milestone: m }),
            onAdd: N
          }
        )
      ] }),
      /* @__PURE__ */ l("aside", { className: "daily-panel", children: f === null ? /* @__PURE__ */ l("p", { className: "daily-faint", children: a("pick") }) : typeof f == "object" ? b[f.milestone] ? /* @__PURE__ */ l(
        Xe,
        {
          projectId: e,
          entry: b[f.milestone],
          readOnly: i,
          onChange: (m) => x(b.map((v, ie) => ie === f.milestone ? m : v)),
          onRemove: () => {
            x(b.filter((m, v) => v !== f.milestone)), u(null);
          }
        }
      ) : /* @__PURE__ */ l("p", { className: "daily-faint", children: a("pick") }) : f === "default" ? /* @__PURE__ */ l(j, { title: a("defaultTitle"), hint: a(p ? "defaultHint" : "defaultNone"), children: p ? /* @__PURE__ */ c(V, { children: [
        /* @__PURE__ */ l(
          z,
          {
            projectId: e,
            reward: p,
            readOnly: i,
            onChange: (m, v) => n(["default_day", ...m], v)
          }
        ),
        /* @__PURE__ */ l("div", { className: "daily-fields", children: /* @__PURE__ */ c("div", { children: [
          /* @__PURE__ */ l("div", { className: "field-label", children: a("stackAllLabel") }),
          /* @__PURE__ */ l("div", { className: "daily-faint", children: a("stackAllHint") }),
          /* @__PURE__ */ l("div", { className: "daily-milestone-kind", children: [!1, !0].map((m) => /* @__PURE__ */ l(
            "button",
            {
              type: "button",
              className: `chip${F(s) === m ? " is-active" : ""}`,
              disabled: i,
              onClick: () => n(["with_default_day"], m),
              children: a(m ? "stackAdd" : "stackReplace")
            },
            String(m)
          )) })
        ] }) }),
        /* @__PURE__ */ l(
          "button",
          {
            type: "button",
            className: "btn btn-ghost btn-sm",
            disabled: i,
            onClick: () => n(["default_day"], void 0),
            children: a("defaultRemove")
          }
        )
      ] }) : /* @__PURE__ */ l(
        "button",
        {
          type: "button",
          className: "btn btn-sm",
          disabled: i,
          onClick: () => n(["default_day"], { icon: { material: "BREAD" }, actions: [] }),
          children: a("defaultAdd")
        }
      ) }) : /* @__PURE__ */ l(
        Je,
        {
          day: f,
          projectId: e,
          own: S(s).get(f),
          fallback: p,
          stacksAll: F(s),
          stacks: G(s, S(s).get(f) ?? {}),
          readOnly: i,
          onCustomize: () => g(f, Ae(p, h) ?? { icon: { material: "CHEST" }, actions: [] }),
          onReset: () => g(f, void 0),
          onChange: (m, v) => n(["days", Be(s, f), ...m], v),
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
  const a = w(), s = P(t, e.reward?.icon), o = ["daily-cell", e.own ? "is-own" : "is-default", n ? "is-selected" : ""];
  return /* @__PURE__ */ c("button", { type: "button", className: o.join(" "), onClick: i, title: a("day", { day: e.day }), children: [
    /* @__PURE__ */ l("span", { className: "daily-cell-day", children: e.day }),
    e.stacks && /* @__PURE__ */ l("span", { className: "daily-cell-stack", title: a("stackBadge"), "aria-label": a("stackBadge"), children: "+" }),
    /* @__PURE__ */ l(C, { reward: e.reward }),
    e.own && s && /* @__PURE__ */ l("span", { className: "daily-cell-title", children: /* @__PURE__ */ l(I, { source: s, maxLines: 2, align: "center", scale: 1 }) })
  ] });
}
function C({ reward: e }) {
  const t = e?.icon, n = xe(t), i = le(n), a = we(t);
  return /* @__PURE__ */ c("span", { className: "daily-slot", children: [
    i ? /* @__PURE__ */ l("img", { src: i, alt: n, draggable: !1 }) : /* @__PURE__ */ l("span", { className: "daily-slot-name", children: n ? n.slice(0, 3) : "—" }),
    a > 1 && /* @__PURE__ */ l("span", { className: "daily-slot-count", children: a })
  ] });
}
function P(e, t) {
  const n = W(e), i = de();
  return ke(t, i, (a) => n ? J(n, a, i) : "");
}
function j({ title: e, hint: t, children: n }) {
  return /* @__PURE__ */ c("div", { className: "daily-panel-body", children: [
    /* @__PURE__ */ c("div", { children: [
      /* @__PURE__ */ l("div", { className: "daily-panel-title", children: e }),
      /* @__PURE__ */ l("div", { className: "daily-faint", children: t })
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
  stacks: s,
  readOnly: o,
  onCustomize: r,
  onReset: p,
  onChange: f,
  onEditDefault: u
}) {
  const d = w(), h = P(t, (n ?? i)?.icon);
  if (!n) {
    const g = Ne(i);
    return /* @__PURE__ */ c(j, { title: d("day", { day: e }), hint: d("inheritHint"), children: [
      /* @__PURE__ */ c("div", { className: "daily-inherited", children: [
        /* @__PURE__ */ l(C, { reward: i }),
        /* @__PURE__ */ c("span", { className: "daily-inherited-text", children: [
          h ? /* @__PURE__ */ l(I, { source: h, scale: 1 }) : /* @__PURE__ */ l("span", { className: "daily-faint", children: d("defaultNone") }),
          /* @__PURE__ */ l("span", { className: "daily-faint", children: g > 0 ? d("steps", { count: g }) : d("noActions") })
        ] })
      ] }),
      /* @__PURE__ */ l("button", { type: "button", className: "btn btn-sm", disabled: o, onClick: r, children: d("customize") }),
      /* @__PURE__ */ l("button", { type: "button", className: "btn btn-ghost btn-sm", onClick: u, children: d("editDefault") })
    ] });
  }
  return /* @__PURE__ */ c(j, { title: d("day", { day: e }), hint: d(i && s ? "ownStackHint" : "ownHint"), children: [
    /* @__PURE__ */ l(z, { projectId: t, reward: n, readOnly: o, onChange: f }),
    i && /* @__PURE__ */ l("fieldset", { className: "daily-fields", disabled: o, children: /* @__PURE__ */ c("div", { children: [
      /* @__PURE__ */ l("div", { className: "field-label", children: d("stackLabel") }),
      /* @__PURE__ */ l("div", { className: "daily-milestone-kind", children: [void 0, !1, !0].map((g) => /* @__PURE__ */ l(
        "button",
        {
          type: "button",
          className: `chip${n.with_default_day === g ? " is-active" : ""}`,
          onClick: () => f(["with_default_day"], g),
          children: g === void 0 ? d("stackFollow", { mode: d(a ? "stackAdd" : "stackReplace") }) : d(g ? "stackAdd" : "stackReplace")
        },
        String(g)
      )) })
    ] }) }),
    /* @__PURE__ */ l("button", { type: "button", className: "btn btn-ghost btn-sm", disabled: o, onClick: p, children: d("reset") })
  ] });
}
function z({
  projectId: e,
  reward: t,
  readOnly: n,
  iconHint: i,
  actionsHint: a,
  onChange: s
}) {
  const o = w();
  return /* @__PURE__ */ c("fieldset", { className: "daily-fields", disabled: n, children: [
    /* @__PURE__ */ c("div", { children: [
      /* @__PURE__ */ l("div", { className: "field-label", children: o("icon") }),
      /* @__PURE__ */ l("div", { className: "daily-faint", children: i ?? o("iconHint") }),
      /* @__PURE__ */ l(re, { projectId: e, value: t.icon, onChange: (r) => s(["icon"], r) })
    ] }),
    /* @__PURE__ */ c("div", { children: [
      /* @__PURE__ */ l("div", { className: "field-label", children: o("actions") }),
      /* @__PURE__ */ l("div", { className: "daily-faint", children: a ?? o("actionsHint") }),
      /* @__PURE__ */ l(
        oe,
        {
          projectId: e,
          value: Array.isArray(t.actions) ? t.actions : [],
          onChange: (r) => s(["actions"], r)
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
  onSelect: s,
  onAdd: o
}) {
  const r = w(), p = A(e), f = U(p, 1, 8).map((u) => u.streak);
  return /* @__PURE__ */ c("section", { className: "daily-milestones", children: [
    /* @__PURE__ */ c("div", { children: [
      /* @__PURE__ */ l("div", { className: "daily-panel-title", children: r("milestonesTitle") }),
      /* @__PURE__ */ l("div", { className: "daily-faint", children: r("milestonesHint") })
    ] }),
    Se(p) && /* @__PURE__ */ l("div", { className: "daily-outside", role: "alert", children: r("milestoneNoRepeat") }),
    t.length === 0 ? /* @__PURE__ */ l("span", { className: "daily-faint", children: r("milestonesNone") }) : /* @__PURE__ */ l("div", { className: "daily-milestone-list", children: t.map((u, d) => /* @__PURE__ */ l(
      Ge,
      {
        entry: u,
        projectId: n,
        selected: i === d,
        onSelect: () => s(d)
      },
      d
    )) }),
    f.length > 0 && /* @__PURE__ */ l("span", { className: "daily-faint", children: r("milestoneNext", { list: f.join(", ") }) }),
    /* @__PURE__ */ c("div", { className: "daily-milestone-add", children: [
      /* @__PURE__ */ l(
        "button",
        {
          type: "button",
          className: "btn btn-sm",
          disabled: a,
          onClick: () => o({ at: 30, icon: { material: "NETHER_STAR" }, actions: [] }),
          children: r("milestoneAddAt")
        }
      ),
      /* @__PURE__ */ l(
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
  const a = w(), s = P(t, e.icon), o = O(e);
  return /* @__PURE__ */ c("button", { type: "button", className: ["daily-default", "daily-milestone", n ? "is-selected" : "", o ? "" : "is-invalid"].join(" "), onClick: i, children: [
    /* @__PURE__ */ l(C, { reward: e }),
    /* @__PURE__ */ c("span", { className: "daily-default-text", children: [
      /* @__PURE__ */ l("span", { className: "daily-default-title", children: o ? Q(o, a) : a("milestoneInvalid") }),
      s && /* @__PURE__ */ l(I, { source: s, maxLines: 1, scale: 1 })
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
  const s = w(), o = O(t), r = t.at === void 0 && t.every !== void 0, p = (u, d) => {
    const h = { ...t, [u]: d };
    d === void 0 && delete h[u];
    const { at: g, every: b, from: x, ...N } = h;
    return Object.fromEntries(
      Object.entries({ at: g, every: b, from: x, ...N }).filter(([, H]) => H !== void 0)
    );
  }, f = (u) => {
    if (u === r) return;
    const { at: d, every: h, from: g, ...b } = t, x = Number(d) || Number(h) || 30;
    i(u ? { every: x, ...b } : { at: x, ...b });
  };
  return /* @__PURE__ */ c(j, { title: o ? Q(o, s) : s("milestoneInvalid"), hint: s("milestonesHint"), children: [
    /* @__PURE__ */ c("fieldset", { className: "daily-fields", disabled: n, children: [
      /* @__PURE__ */ c("div", { children: [
        /* @__PURE__ */ l("div", { className: "field-label", children: s("milestoneKind") }),
        /* @__PURE__ */ c("div", { className: "daily-milestone-kind", children: [
          /* @__PURE__ */ l("button", { type: "button", className: `chip${r ? "" : " is-active"}`, onClick: () => f(!1), children: s("milestoneOnce") }),
          /* @__PURE__ */ l("button", { type: "button", className: `chip${r ? " is-active" : ""}`, onClick: () => f(!0), children: s("milestoneRepeat") })
        ] })
      ] }),
      r ? /* @__PURE__ */ c(V, { children: [
        /* @__PURE__ */ c("label", { children: [
          /* @__PURE__ */ l("span", { className: "field-label", children: s("milestoneEveryLabel") }),
          /* @__PURE__ */ l(
            "input",
            {
              className: "input",
              type: "number",
              min: 1,
              value: String(t.every ?? ""),
              onChange: (u) => i(p("every", M(u.target.value)))
            }
          )
        ] }),
        /* @__PURE__ */ c("label", { children: [
          /* @__PURE__ */ l("span", { className: "field-label", children: s("milestoneFromLabel") }),
          /* @__PURE__ */ l("span", { className: "daily-faint", children: s("milestoneFromHint") }),
          /* @__PURE__ */ l(
            "input",
            {
              className: "input",
              type: "number",
              min: 1,
              value: String(t.from ?? ""),
              placeholder: String(t.every ?? ""),
              onChange: (u) => i(p("from", M(u.target.value)))
            }
          )
        ] })
      ] }) : /* @__PURE__ */ c("label", { children: [
        /* @__PURE__ */ l("span", { className: "field-label", children: s("milestoneAtLabel") }),
        /* @__PURE__ */ l(
          "input",
          {
            className: "input",
            type: "number",
            min: 1,
            value: String(t.at ?? ""),
            onChange: (u) => i(p("at", M(u.target.value)))
          }
        )
      ] })
    ] }),
    /* @__PURE__ */ l(
      z,
      {
        projectId: e,
        reward: t,
        readOnly: n,
        iconHint: s("milestoneIconHint"),
        actionsHint: s("milestoneActionsHint"),
        onChange: (u, d) => i(p(u[0], d))
      }
    ),
    /* @__PURE__ */ l("button", { type: "button", className: "btn btn-ghost btn-sm", disabled: n, onClick: a, children: s("milestoneRemove") })
  ] });
}
const Qe = `
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
.daily-cell-stack { position: absolute; top: 3px; right: 6px; font-size: 12px; font-weight: 700; line-height: 1;
  color: var(--fam-reward); }
.daily-legend-stack::before { display: none; }
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
  localPreviewRows: ze
}, at = { ...Ze, sdk: "1.20.0" };
export {
  at as default
};
