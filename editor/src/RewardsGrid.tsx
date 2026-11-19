import {
  ActionChainEditor,
  type ConfigScreenProps,
  ItemValueInput,
  McText,
  projectLangs,
  textValue,
  useContentLang,
  useItemTexture,
  useProject,
} from "@artisan/editor-sdk";
import { type ReactNode, useState } from "react";
import { useT } from "./i18n";
import {
  type Cell,
  type Reward,
  type RewardsValue,
  WEEK,
  actionCount,
  asObject,
  cells,
  cycleLength,
  daysOutside,
  detachTexts,
  iconCount,
  iconMaterial,
  iconTitle,
  milestoneEntries,
  milestoneRule,
  milestones,
  noneRepeats,
  ownDays,
  readMilestone,
  stacksDefault,
  upcoming,
  withDay,
  withDefaultDay,
} from "./rewards";

/** A day of the cycle, the other days, or the milestone at that place of `milestones`. */
type Selection = number | "default" | { milestone: number } | null;

/**
 * The Rewards screen of the daily folder: the cycle as a grid of weeks. A day with its own
 * reward stands out, the others show the reward of the other days, faded. A click opens the
 * day beside the grid: its icon and actions, or the button that gives it its own reward. Under
 * the grid, the streak milestones: a click edits one beside the grid too.
 */
export function RewardsGrid({ projectId, value, set, readOnly }: ConfigScreenProps) {
  const t = useT();
  const rewards = (asObject(value) ?? {}) as RewardsValue;
  const grid = cells(rewards);
  const outside = daysOutside(rewards);
  const fallback = asObject(rewards.default_day);
  const [selected, setSelected] = useState<Selection>(null);
  const project = useProject(projectId);
  // A literal title is the default language's text, as the generated form writes it.
  const defaultText = (key: string) =>
    project ? textValue(project, key, projectLangs(project)[0] ?? "en") : "";

  const setDay = (day: number, reward: Reward | undefined) => set(["days"], withDay(rewards, day, reward));
  const entries = milestoneEntries(rewards);
  const setMilestones = (next: Reward[]) => set(["milestones"], next.length > 0 ? next : undefined);
  const addMilestone = (entry: Reward) => {
    setMilestones([...entries, entry]);
    setSelected({ milestone: entries.length });
  };
  const selectedMilestone = typeof selected === "object" && selected !== null ? selected.milestone : -1;

  return (
    <div className="daily-rewards">
      <div className="daily-toolbar">
        <label className="daily-cycle">
          <span className="field-label">{t("cycleLength")}</span>
          <input
            className="input daily-cycle-input"
            type="number"
            min={1}
            value={cycleLength(rewards)}
            disabled={readOnly}
            onChange={(e) => {
              const n = Number.parseInt(e.target.value, 10);
              if (Number.isInteger(n) && n >= 1) set(["cycle_length"], n);
            }}
          />
          <span className="daily-faint">{t("cycleHint")}</span>
        </label>
        <div className="daily-legend">
          <span className="daily-legend-own">{t("legendOwn")}</span>
          <span className="daily-legend-default">{t("legendDefault")}</span>
          {grid.some((cell) => cell.stacks) && <span className="daily-legend-stack">{t("legendStack")}</span>}
        </div>
      </div>

      {outside.length > 0 && (
        <div className="daily-outside" role="alert">
          <strong>{t("outsideTitle")}</strong>
          <span>{t("outsideHint")}</span>
          <div className="daily-outside-days">
            {outside.map((day) => (
              <button
                key={day}
                type="button"
                className="chip"
                disabled={readOnly}
                onClick={() => setDay(day, undefined)}
              >
                {t("remove", { day })} ×
              </button>
            ))}
          </div>
        </div>
      )}

      <div className="daily-layout">
        <div className="daily-board">
          <button
            type="button"
            className={`daily-default${selected === "default" ? " is-selected" : ""}`}
            onClick={() => setSelected("default")}
          >
            <Slot reward={fallback} />
            <span className="daily-default-text">
              <span className="daily-default-title">{t("defaultTitle")}</span>
              <span className="daily-faint">{fallback ? t("defaultHint") : t("defaultNone")}</span>
            </span>
          </button>
          <div className="daily-grid" style={{ gridTemplateColumns: `repeat(${WEEK}, minmax(0, 1fr))` }}>
            {grid.map((cell) => (
              <DayCell
                key={cell.day}
                cell={cell}
                projectId={projectId}
                selected={selected === cell.day}
                onSelect={() => setSelected(cell.day)}
              />
            ))}
          </div>
          <MilestoneList
            rewards={rewards}
            entries={entries}
            projectId={projectId}
            selected={selectedMilestone}
            readOnly={readOnly}
            onSelect={(index) => setSelected({ milestone: index })}
            onAdd={addMilestone}
          />
        </div>

        <aside className="daily-panel">
          {selected === null ? (
            <p className="daily-faint">{t("pick")}</p>
          ) : typeof selected === "object" ? (
            entries[selected.milestone] ? (
              <MilestonePanel
                projectId={projectId}
                entry={entries[selected.milestone]}
                readOnly={readOnly}
                onChange={(next) => setMilestones(entries.map((e, i) => (i === selected.milestone ? next : e)))}
                onRemove={() => {
                  setMilestones(entries.filter((_, i) => i !== selected.milestone));
                  setSelected(null);
                }}
              />
            ) : (
              <p className="daily-faint">{t("pick")}</p>
            )
          ) : selected === "default" ? (
            <Panel title={t("defaultTitle")} hint={fallback ? t("defaultHint") : t("defaultNone")}>
              {fallback ? (
                <>
                  <RewardFields
                    projectId={projectId}
                    reward={fallback}
                    readOnly={readOnly}
                    onChange={(path, next) => set(["default_day", ...path], next)}
                  />
                  <div className="daily-fields">
                    <div>
                      <div className="field-label">{t("stackAllLabel")}</div>
                      <div className="daily-faint">{t("stackAllHint")}</div>
                      <div className="daily-milestone-kind">
                        {[false, true].map((stack) => (
                          <button
                            key={String(stack)}
                            type="button"
                            className={`chip${withDefaultDay(rewards) === stack ? " is-active" : ""}`}
                            disabled={readOnly}
                            onClick={() => set(["with_default_day"], stack)}
                          >
                            {t(stack ? "stackAdd" : "stackReplace")}
                          </button>
                        ))}
                      </div>
                    </div>
                  </div>
                  <button
                    type="button"
                    className="btn btn-ghost btn-sm"
                    disabled={readOnly}
                    onClick={() => set(["default_day"], undefined)}
                  >
                    {t("defaultRemove")}
                  </button>
                </>
              ) : (
                <button
                  type="button"
                  className="btn btn-sm"
                  disabled={readOnly}
                  onClick={() => set(["default_day"], { icon: { material: "BREAD" }, actions: [] })}
                >
                  {t("defaultAdd")}
                </button>
              )}
            </Panel>
          ) : (
            <DayPanel
              day={selected}
              projectId={projectId}
              own={ownDays(rewards).get(selected)}
              fallback={fallback}
              stacksAll={withDefaultDay(rewards)}
              stacks={stacksDefault(rewards, ownDays(rewards).get(selected) ?? {})}
              readOnly={readOnly}
              onCustomize={() =>
                setDay(selected, detachTexts(fallback, defaultText) ?? { icon: { material: "CHEST" }, actions: [] })
              }
              onReset={() => setDay(selected, undefined)}
              onChange={(path, next) => set(["days", dayKey(rewards, selected), ...path], next)}
              onEditDefault={() => setSelected("default")}
            />
          )}
        </aside>
      </div>
    </div>
  );
}

/** The key `days` holds the day under, as written (`7` or `"7"`). */
function dayKey(value: RewardsValue, day: number): string {
  return Object.keys(asObject(value.days) ?? {}).find((k) => Number(k) === day) ?? String(day);
}

function DayCell({
  cell,
  projectId,
  selected,
  onSelect,
}: {
  cell: Cell;
  projectId: string;
  selected: boolean;
  onSelect: () => void;
}) {
  const t = useT();
  const title = useIconTitle(projectId, cell.reward?.icon);
  const classes = ["daily-cell", cell.own ? "is-own" : "is-default", selected ? "is-selected" : ""];
  return (
    <button type="button" className={classes.join(" ")} onClick={onSelect} title={t("day", { day: cell.day })}>
      <span className="daily-cell-day">{cell.day}</span>
      {cell.stacks && (
        <span className="daily-cell-stack" title={t("stackBadge")} aria-label={t("stackBadge")}>
          +
        </span>
      )}
      <Slot reward={cell.reward} />
      {cell.own && title && (
        <span className="daily-cell-title">
          <McText source={title} maxLines={2} align="center" scale={1} />
        </span>
      )}
    </button>
  );
}

/** An inventory slot with the reward's icon and its count. */
function Slot({ reward }: { reward: Reward | undefined }) {
  const icon = reward?.icon;
  const material = iconMaterial(icon);
  const texture = useItemTexture(material);
  const count = iconCount(icon);
  return (
    <span className="daily-slot">
      {texture ? (
        <img src={texture} alt={material} draggable={false} />
      ) : (
        <span className="daily-slot-name">{material ? material.slice(0, 3) : "—"}</span>
      )}
      {count > 1 && <span className="daily-slot-count">{count}</span>}
    </span>
  );
}

function useIconTitle(projectId: string, icon: unknown): string {
  const project = useProject(projectId);
  const lang = useContentLang();
  return iconTitle(icon, lang, (key) => (project ? textValue(project, key, lang) : ""));
}

function Panel({ title, hint, children }: { title: string; hint: string; children: ReactNode }) {
  return (
    <div className="daily-panel-body">
      <div>
        <div className="daily-panel-title">{title}</div>
        <div className="daily-faint">{hint}</div>
      </div>
      {children}
    </div>
  );
}

function DayPanel({
  day,
  projectId,
  own,
  fallback,
  stacksAll,
  stacks,
  readOnly,
  onCustomize,
  onReset,
  onChange,
  onEditDefault,
}: {
  day: number;
  projectId: string;
  own: Reward | undefined;
  fallback: Reward | undefined;
  /** `with_default_day` of the file. */
  stacksAll: boolean;
  /** This day also pays `default_day` (its own setting, else the file's). */
  stacks: boolean;
  readOnly: boolean;
  onCustomize: () => void;
  onReset: () => void;
  onChange: (path: string[], value: unknown) => void;
  onEditDefault: () => void;
}) {
  const t = useT();
  const title = useIconTitle(projectId, (own ?? fallback)?.icon);
  if (!own) {
    const steps = actionCount(fallback);
    return (
      <Panel title={t("day", { day })} hint={t("inheritHint")}>
        <div className="daily-inherited">
          <Slot reward={fallback} />
          <span className="daily-inherited-text">
            {title ? <McText source={title} scale={1} /> : <span className="daily-faint">{t("defaultNone")}</span>}
            <span className="daily-faint">{steps > 0 ? t("steps", { count: steps }) : t("noActions")}</span>
          </span>
        </div>
        <button type="button" className="btn btn-sm" disabled={readOnly} onClick={onCustomize}>
          {t("customize")}
        </button>
        <button type="button" className="btn btn-ghost btn-sm" onClick={onEditDefault}>
          {t("editDefault")}
        </button>
      </Panel>
    );
  }
  return (
    <Panel title={t("day", { day })} hint={t(fallback && stacks ? "ownStackHint" : "ownHint")}>
      <RewardFields projectId={projectId} reward={own} readOnly={readOnly} onChange={onChange} />
      {fallback && (
        <fieldset className="daily-fields" disabled={readOnly}>
          <div>
            <div className="field-label">{t("stackLabel")}</div>
            <div className="daily-milestone-kind">
              {([undefined, false, true] as const).map((choice) => (
                <button
                  key={String(choice)}
                  type="button"
                  className={`chip${own.with_default_day === choice ? " is-active" : ""}`}
                  onClick={() => onChange(["with_default_day"], choice)}
                >
                  {choice === undefined
                    ? t("stackFollow", { mode: t(stacksAll ? "stackAdd" : "stackReplace") })
                    : t(choice ? "stackAdd" : "stackReplace")}
                </button>
              ))}
            </div>
          </div>
        </fieldset>
      )}
      <button type="button" className="btn btn-ghost btn-sm" disabled={readOnly} onClick={onReset}>
        {t("reset")}
      </button>
    </Panel>
  );
}

function RewardFields({
  projectId,
  reward,
  readOnly,
  iconHint,
  actionsHint,
  onChange,
}: {
  projectId: string;
  reward: Reward;
  readOnly: boolean;
  iconHint?: string;
  actionsHint?: string;
  onChange: (path: string[], value: unknown) => void;
}) {
  const t = useT();
  return (
    <fieldset className="daily-fields" disabled={readOnly}>
      <div>
        <div className="field-label">{t("icon")}</div>
        <div className="daily-faint">{iconHint ?? t("iconHint")}</div>
        <ItemValueInput projectId={projectId} value={reward.icon} onChange={(next) => onChange(["icon"], next)} />
      </div>
      <div>
        <div className="field-label">{t("actions")}</div>
        <div className="daily-faint">{actionsHint ?? t("actionsHint")}</div>
        <ActionChainEditor
          projectId={projectId}
          value={Array.isArray(reward.actions) ? reward.actions : []}
          onChange={(next) => onChange(["actions"], next)}
        />
      </div>
    </fieldset>
  );
}

/** The streak milestones under the grid, each a card; the add buttons; the warnings. */
function MilestoneList({
  rewards,
  entries,
  projectId,
  selected,
  readOnly,
  onSelect,
  onAdd,
}: {
  rewards: RewardsValue;
  entries: Reward[];
  projectId: string;
  selected: number;
  readOnly: boolean;
  onSelect: (index: number) => void;
  onAdd: (entry: Reward) => void;
}) {
  const t = useT();
  const read = milestones(rewards);
  const next = upcoming(read, 1, 8).map((o) => o.streak);
  return (
    <section className="daily-milestones">
      <div>
        <div className="daily-panel-title">{t("milestonesTitle")}</div>
        <div className="daily-faint">{t("milestonesHint")}</div>
      </div>
      {noneRepeats(read) && (
        <div className="daily-outside" role="alert">
          {t("milestoneNoRepeat")}
        </div>
      )}
      {entries.length === 0 ? (
        <span className="daily-faint">{t("milestonesNone")}</span>
      ) : (
        <div className="daily-milestone-list">
          {entries.map((entry, index) => (
            <MilestoneCard
              key={index}
              entry={entry}
              projectId={projectId}
              selected={selected === index}
              onSelect={() => onSelect(index)}
            />
          ))}
        </div>
      )}
      {next.length > 0 && <span className="daily-faint">{t("milestoneNext", { list: next.join(", ") })}</span>}
      <div className="daily-milestone-add">
        <button
          type="button"
          className="btn btn-sm"
          disabled={readOnly}
          onClick={() => onAdd({ at: 30, icon: { material: "NETHER_STAR" }, actions: [] })}
        >
          {t("milestoneAddAt")}
        </button>
        <button
          type="button"
          className="btn btn-sm"
          disabled={readOnly}
          onClick={() => onAdd({ every: 50, icon: { material: "NETHER_STAR" }, actions: [] })}
        >
          {t("milestoneAddEvery")}
        </button>
      </div>
    </section>
  );
}

function MilestoneCard({
  entry,
  projectId,
  selected,
  onSelect,
}: {
  entry: Reward;
  projectId: string;
  selected: boolean;
  onSelect: () => void;
}) {
  const t = useT();
  const title = useIconTitle(projectId, entry.icon);
  const m = readMilestone(entry);
  const classes = ["daily-default", "daily-milestone", selected ? "is-selected" : "", m ? "" : "is-invalid"];
  return (
    <button type="button" className={classes.join(" ")} onClick={onSelect}>
      <Slot reward={entry} />
      <span className="daily-default-text">
        <span className="daily-default-title">{m ? milestoneRule(m, t) : t("milestoneInvalid")}</span>
        {title && <McText source={title} maxLines={1} scale={1} />}
      </span>
    </button>
  );
}

/** A whole number from 1 typed in `value`, else undefined (the key leaves the file). */
function wholeOrUndefined(value: string): number | undefined {
  const n = Number.parseInt(value, 10);
  return Number.isInteger(n) && n >= 1 ? n : undefined;
}

function MilestonePanel({
  projectId,
  entry,
  readOnly,
  onChange,
  onRemove,
}: {
  projectId: string;
  entry: Reward;
  readOnly: boolean;
  onChange: (next: Reward) => void;
  onRemove: () => void;
}) {
  const t = useT();
  const m = readMilestone(entry);
  const repeats = entry.at === undefined && entry.every !== undefined;
  /** The entry with `key` set (or removed when undefined), its keys in the file's usual order. */
  const withKey = (key: string, value: unknown): Reward => {
    const next: Reward = { ...entry, [key]: value };
    if (value === undefined) delete next[key];
    const { at, every, from, ...rest } = next;
    return Object.fromEntries(
      Object.entries({ at, every, from, ...rest }).filter(([, v]) => v !== undefined),
    ) as Reward;
  };
  const setKind = (repeat: boolean) => {
    if (repeat === repeats) return;
    const { at, every, from, ...rest } = entry;
    const step = Number(at) || Number(every) || 30;
    onChange(repeat ? { every: step, ...rest } : { at: step, ...rest });
  };
  return (
    <Panel title={m ? milestoneRule(m, t) : t("milestoneInvalid")} hint={t("milestonesHint")}>
      <fieldset className="daily-fields" disabled={readOnly}>
        <div>
          <div className="field-label">{t("milestoneKind")}</div>
          <div className="daily-milestone-kind">
            <button type="button" className={`chip${repeats ? "" : " is-active"}`} onClick={() => setKind(false)}>
              {t("milestoneOnce")}
            </button>
            <button type="button" className={`chip${repeats ? " is-active" : ""}`} onClick={() => setKind(true)}>
              {t("milestoneRepeat")}
            </button>
          </div>
        </div>
        {repeats ? (
          <>
            <label>
              <span className="field-label">{t("milestoneEveryLabel")}</span>
              <input
                className="input"
                type="number"
                min={1}
                value={String(entry.every ?? "")}
                onChange={(e) => onChange(withKey("every", wholeOrUndefined(e.target.value)))}
              />
            </label>
            <label>
              <span className="field-label">{t("milestoneFromLabel")}</span>
              <span className="daily-faint">{t("milestoneFromHint")}</span>
              <input
                className="input"
                type="number"
                min={1}
                value={String(entry.from ?? "")}
                placeholder={String(entry.every ?? "")}
                onChange={(e) => onChange(withKey("from", wholeOrUndefined(e.target.value)))}
              />
            </label>
          </>
        ) : (
          <label>
            <span className="field-label">{t("milestoneAtLabel")}</span>
            <input
              className="input"
              type="number"
              min={1}
              value={String(entry.at ?? "")}
              onChange={(e) => onChange(withKey("at", wholeOrUndefined(e.target.value)))}
            />
          </label>
        )}
      </fieldset>
      <RewardFields
        projectId={projectId}
        reward={entry}
        readOnly={readOnly}
        iconHint={t("milestoneIconHint")}
        actionsHint={t("milestoneActionsHint")}
        onChange={(path, next) => onChange(withKey(path[0], next))}
      />
      <button type="button" className="btn btn-ghost btn-sm" disabled={readOnly} onClick={onRemove}>
        {t("milestoneRemove")}
      </button>
    </Panel>
  );
}
