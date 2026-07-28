import fs from "node:fs/promises";
import path from "node:path";
import { Presentation, PresentationFile } from "@oai/artifact-tool";

import { buildSlide01 } from "./template/slide-01.mjs";
import { buildSlide05 } from "./template/slide-05.mjs";
import { buildSlide06 } from "./template/slide-06.mjs";
import { buildSlide13 } from "./template/slide-13.mjs";
import { buildSlide14 } from "./template/slide-14.mjs";
import { buildSlide16 } from "./template/slide-16.mjs";
import { buildSlide17 } from "./template/slide-17.mjs";
import { buildSlide18 } from "./template/slide-18.mjs";
import { buildSlide26 } from "./template/slide-26.mjs";

const OUTPUT_DIR = path.resolve("E:/Mad/presentation_build/output");
const FINAL_PPTX = path.resolve("E:/Mad/AbroadIQ_Project_Presentation.pptx");
const PROPOSAL = "E:/FYP/Abroadiq Project Proposal.docx";
const REPO = "E:/Mad";

await fs.mkdir(OUTPUT_DIR, { recursive: true });

const deck = Presentation.create({
  slideSize: { width: 1280, height: 720 },
});

function textCard(title, body, titleKey = "titleHere") {
  return {
    [titleKey]: title,
    loremIpsumDolorSitAmetConsecteturAdipiscing: body,
  };
}

function sources(items) {
  return [
    "",
    "[Sources]",
    ...items.map((item) => `- ${item}`),
  ].join("\n");
}

function addSlide(builder, tokens, notes, sourceItems) {
  const slide = builder(deck, tokens);
  slide.speakerNotes.textFrame.setText(`${notes}${sources(sourceItems)}`);
  slide.speakerNotes.setVisible(true);
  return slide;
}

addSlide(
  buildSlide01,
  {
    title: "SOFTWARE ENGINEERING · UMT",
    title2: "AbroadIQ\nAI-powered global education guidance",
    title3:
      "Muhammad Zohaib · Muhammad Huzaifa\nMuhammad Sharafat Ali · Shahryar Nadeem\nSupervisor: Mam Insha Rafique",
  },
  "Open with the core idea: AbroadIQ brings fragmented study-abroad guidance, preparation, and application support into one mobile-first product.",
  [PROPOSAL],
);

addSlide(
  buildSlide13,
  {
    title: "The problem: ambition is high, guidance is fragmented",
    body1: textCard(
      "Information overload",
      "University, visa, cost, scholarship, and eligibility data is scattered across portals and agencies.",
      "titleGoesHere",
    ),
    body2: textCard(
      "Generic preparation",
      "Static question banks encourage memorization and provide little adaptive feedback.",
      "titleGoesHere",
    ),
    body3: textCard(
      "Missed opportunities",
      "Students discover scholarships late, misunderstand eligibility, or miss critical deadlines.",
      "titleGoesHere",
    ),
    body4: textCard(
      "Low-confidence decisions",
      "Students lack personalized support for SOPs, interviews, and realistic university choices.",
      "titleGoesHere",
    ),
    footer1: "02",
  },
  "Frame the problem around decision quality, preparation quality, and timing—not only access to information.",
  [PROPOSAL],
);

addSlide(
  buildSlide05,
  {
    title: "Solution and scope",
    body1: textCard(
      "One guided journey",
      "Discover universities and scholarships.\n\nEvaluate profile fit.\n\nPrepare for tests and visa interviews.\n\nBuild SOPs and a personalized roadmap.",
    ),
    body2: textCard(
      "Phase-one boundary",
      "Mobile-first Android experience for South Asian applicants targeting the UK, USA, Canada, and Australia.\n\nCloud data, AI-assisted guidance, reminders, analytics, and consultant support.",
    ),
    footer1: "03",
  },
  "Emphasize that AbroadIQ is an integrated decision-and-preparation platform. It supports the student journey; it does not replace official university or immigration decisions.",
  [PROPOSAL],
);

addSlide(
  buildSlide16,
  {
    title: "Product scope: eight connected capability areas",
    body1: textCard("University finder", "Search, filters, cards, costs, rankings, and program details."),
    body2: textCard("AI eligibility", "Profile-based Safe, Target, and Ambitious recommendations."),
    body3: textCard("Scholarship radar", "Profile matching, favourites, opening dates, and reminders."),
    body4: textCard("Test preparation", "IELTS, PTE, SAT, English Foundation, progress, and mock tests."),
    body5: textCard("Visa interview", "Speech-to-text, dynamic questions, AI evaluation, and coaching."),
    body6: textCard("SOP engine", "Structured profile input, generated drafts, and improvement guidance."),
    body7: textCard("Roadmap", "Country-specific milestones, checklists, and deadline tracking."),
    body8: textCard("Human support", "Consultant chat/calls, community, chatbot, and reports."),
    footer1: "04",
  },
  "Use this as the product map. In the live demo, select only the strongest connected journey rather than opening every screen.",
  [
    PROPOSAL,
    `${REPO}/app/src/main/java/com/example/madfinalproject`,
    `${REPO}/app/src/main/res/layout`,
  ],
);

addSlide(
  buildSlide06,
  {
    title: "Domain and market position",
    body1: textCard(
      "Domain",
      "EdTech + study-abroad guidance\n\nDecision support, preparation, workflow, and student success.",
    ),
    body2: textCard(
      "Primary users",
      "Budget-conscious undergraduate and graduate applicants in South Asia.\n\nMobile-first; targeting major English-speaking destinations.",
    ),
    body3: textCard(
      "Positioning",
      "AbroadIQ connects discovery, preparation, visa practice, SOP support, scholarships, and progress in one student journey.",
    ),
    footer1: "05",
  },
  "Competitors tend to be strongest in one or two stages. AbroadIQ's intended differentiation is workflow integration for South Asian applicants, not an unsupported claim of being universally better.",
  [
    PROPOSAL,
    "https://yocket.com/",
    "https://www.applyboard.com/",
    "https://www.idp.com/",
    "https://ielts.magoosh.com/",
  ],
);

addSlide(
  buildSlide14,
  {
    title: "Competitive landscape",
    body1: {
      topic: "Feature emphasis across representative platforms",
      loremIpsumDolorSitAmetConsecteturAdipiscing:
        "AbroadIQ’s proposed advantage is an integrated mobile workflow; competitor capabilities are summarized from official product pages.",
    },
    footer1: "06",
  },
  "Explain the matrix as feature emphasis, not a certification of exhaustive coverage. Use official pages if challenged on competitor scope.",
  [
    "https://yocket.com/",
    "https://www.applyboard.com/",
    "https://www.idp.com/",
    "https://ielts.magoosh.com/",
  ],
);

addSlide(
  buildSlide06,
  {
    title: "Actors in the AbroadIQ ecosystem",
    body1: textCard(
      "Student / aspirant",
      "Creates a profile, explores options, prepares, saves scholarships, tracks progress, and requests support.",
    ),
    body2: textCard(
      "People and content roles",
      "Consultant: advises through chat/call.\n\nAdmin / content manager: manages data, moderation, and learning content.",
    ),
    body3: textCard(
      "System actors",
      "AI services: recommendations and evaluation.\n\nFirebase + notifications: identity, data, history, reminders, and messaging.",
    ),
    footer1: "07",
  },
  "Distinguish human actors from external system actors. The student is the primary business actor.",
  [PROPOSAL, `${REPO}/app/src/main/AndroidManifest.xml`],
);

addSlide(
  buildSlide16,
  {
    title: "Product backlog: epics",
    body1: textCard("E1 · Identity & profile", "Authentication, qualifications, goals, budget, and profile completion."),
    body2: textCard("E2 · Discovery & fit", "Universities, programs, costs, filters, recommendations, eligibility."),
    body3: textCard("E3 · Scholarships", "Eligibility matching, favourites, rolling dates, and reminders."),
    body4: textCard("E4 · Test preparation", "IELTS, PTE, SAT, English Foundation, quizzes, and progress."),
    body5: textCard("E5 · Visa interview", "Country topics, dynamic sessions, speech, evaluation, and reports."),
    body6: textCard("E6 · SOP & documents", "Profile intake, SOP generation, editing, and document guidance."),
    body7: textCard("E7 · Roadmap & experts", "Milestones, consultant chat/call, and application guidance."),
    body8: textCard("E8 · Community & insights", "Posts, comments, chatbot, dashboards, notifications, analytics."),
    footer1: "08",
  },
  "These epics normalize the proposal and current implementation into a Jira-ready product backlog. Confirm final naming against the team's actual Jira board before presenting.",
  [PROPOSAL, `${REPO}/app/src/main/java/com/example/madfinalproject`],
);

addSlide(
  buildSlide06,
  {
    title: "User stories: discovery, fit, and funding",
    body1: textCard(
      "Discover",
      "As a student, I want to filter universities and inspect programs, fees, rankings, and scholarships so that I can build a realistic shortlist.",
    ),
    body2: textCard(
      "Evaluate",
      "As a student, I want my profile classified against university requirements so that I can compare Safe, Target, and Ambitious options.",
    ),
    body3: textCard(
      "Fund",
      "As a student, I want eligible scholarships, favourites, and opening reminders so that I do not miss funding opportunities.",
    ),
    footer1: "09",
  },
  "In Jira, each story should include acceptance criteria, priority, estimate, assignee, and sprint.",
  [PROPOSAL, `${REPO}/app/src/main/java/com/example/madfinalproject/recommendations`, `${REPO}/app/src/main/java/com/example/madfinalproject/scholarships`],
);

addSlide(
  buildSlide13,
  {
    title: "User stories: preparation, guidance, and progress",
    body1: textCard(
      "Prepare",
      "As a student, I want structured IELTS, PTE, SAT, and English lessons with mock tests so that I can improve by skill.",
      "titleGoesHere",
    ),
    body2: textCard(
      "Practice interviews",
      "As a student, I want varied visa questions, live transcripts, and structured feedback so that I can answer confidently.",
      "titleGoesHere",
    ),
    body3: textCard(
      "Build an application",
      "As a student, I want SOP guidance, checklists, and a country roadmap so that I can complete the right steps in sequence.",
      "titleGoesHere",
    ),
    body4: textCard(
      "Get support",
      "As a student, I want consultant, community, chatbot, and progress views so that help and evidence remain accessible.",
      "titleGoesHere",
    ),
    footer1: "10",
  },
  "Select one acceptance test per story for the demonstration: input, expected behavior, and visible result.",
  [PROPOSAL, `${REPO}/app/src/main/java/com/example/madfinalproject`],
);

addSlide(
  buildSlide18,
  {
    title: "Sprint backlog summary · foundation to recommendations",
    label1: "SPRINT 0",
    label2: "SPRINT 1",
    label3: "SPRINT 2",
    body1: textCard(
      "Platform foundation",
      "Project setup, Firebase, authentication, profile capture, navigation, shared UI, and data models.",
    ),
    body2: textCard(
      "University discovery",
      "Explore, programs, costs, filters, recommendations, and AI eligibility results.",
    ),
    body3: textCard(
      "Funding and roadmap",
      "Scholarships, favourites, date rollover, reminders, country roadmap, and progress.",
    ),
    footer1: "11",
  },
  "This sprint grouping is reconstructed from the proposal and current codebase. Replace sprint labels, dates, velocity, and completion status with the exact Jira data used by the team.",
  [PROPOSAL, `${REPO}/app/src/main/java/com/example/madfinalproject`],
);

addSlide(
  buildSlide18,
  {
    title: "Sprint backlog summary · preparation to integration",
    label1: "SPRINT 3",
    label2: "SPRINT 4",
    label3: "SPRINT 5",
    body1: textCard(
      "Test preparation",
      "IELTS, PTE, SAT, English Foundation grammar and vocabulary, mock tests, and progress.",
    ),
    body2: textCard(
      "AI visa interview",
      "Question engine, SpeechRecognizer, evaluation, adaptive flow, coaching, and reports.",
    ),
    body3: textCard(
      "Guidance and release",
      "SOP, consultant chat/call, community, chatbot, integration, quality checks, and demo packaging.",
    ),
    footer1: "12",
  },
  "Do not present this reconstruction as a Jira export. Align the slide with the actual sprint board and show completed vs pending items live.",
  [PROPOSAL, `${REPO}/app/src/main/java/com/example/madfinalproject`],
);

addSlide(
  buildSlide13,
  {
    title: "Team accountability must be visible in Jira",
    body1: textCard(
      "Muhammad Zohaib",
      "Open assigned epic/story, acceptance criteria, sprint, status, and recent activity during the live Jira demo.",
      "titleGoesHere",
    ),
    body2: textCard(
      "Muhammad Huzaifa",
      "Open assigned epic/story, acceptance criteria, sprint, status, and recent activity during the live Jira demo.",
      "titleGoesHere",
    ),
    body3: textCard(
      "Muhammad Sharafat Ali",
      "Open assigned epic/story, acceptance criteria, sprint, status, and recent activity during the live Jira demo.",
      "titleGoesHere",
    ),
    body4: textCard(
      "Shahryar Nadeem",
      "Open assigned epic/story, acceptance criteria, sprint, status, and recent activity during the live Jira demo.",
      "titleGoesHere",
    ),
    footer1: "13",
  },
  "The proposal confirms team membership but not ownership. Do not invent assignments. Before defense, ensure the Jira board shows real assignees, story links, estimates, and transitions for every member.",
  [PROPOSAL],
);

addSlide(
  buildSlide06,
  {
    title: "First demo: project collaboration evidence",
    body1: textCard(
      "Jira",
      "Show epics → user stories → acceptance criteria.\n\nFilter by sprint and assignee.\n\nOpen one completed story per member.",
    ),
    body2: textCard(
      "GitHub",
      "Open repository and commit history.\n\nShow unique authors, meaningful messages, branches or pull requests.\n\nMap commits to Jira work.",
    ),
    body3: textCard(
      "Team communication",
      "Open Slack, Teams, or an approved alternative.\n\nShow channels, decisions, file sharing, and coordination—not private content.",
    ),
    footer1: "14",
  },
  "Important readiness check: the local repository currently exposes only one committed author in its visible history. Before the presentation, every member must push genuine work using their own Git identity. Do not fabricate commits or claim four contributors until GitHub confirms them.",
  [
    "https://github.com/MZoHaiB142/MadFinalProject",
    `${REPO}/.git`,
    "Local audit performed with git log and git shortlog on 2026-07-28",
  ],
);

addSlide(
  buildSlide17,
  {
    title: "Second demo: working product architecture",
    label1: "CLIENT",
    label2: "CLOUD",
    label3: "INTELLIGENCE",
    body1: textCard(
      "Android Native",
      "Java + XML application on physical device or emulator. Student journey and local UI remain demonstrable.",
    ),
    body2: textCard(
      "Firebase",
      "Authentication, Firestore / Realtime Database, Storage, notifications, history, and shared state.",
    ),
    body3: textCard(
      "AI and speech services",
      "Android SpeechRecognizer plus configured Gemini / OpenAI services for recommendations, evaluation, and guidance.",
    ),
    footer1: "15",
  },
  "Describe this as a cloud-backed Android product. Never expose API keys during the demonstration; keep them in secure configuration and restrict them.",
  [
    PROPOSAL,
    `${REPO}/app/build.gradle.kts`,
    `${REPO}/app/src/main/AndroidManifest.xml`,
    `${REPO}/app/src/main/java/com/example/madfinalproject`,
  ],
);

addSlide(
  buildSlide05,
  {
    title: "Second demo: cloud route and offline contingency",
    body1: textCard(
      "Primary · cloud-backed demo",
      "1. Sign in with prepared test account.\n2. Complete profile.\n3. Open AI eligibility and shortlist.\n4. Save a scholarship.\n5. Run a visa or test-prep flow.\n6. Show persisted progress.",
    ),
    body2: textCard(
      "Fallback · local execution",
      "Keep a tested debug/signed APK, Android Studio project, emulator, and physical device ready.\n\nCache or bundle demo-safe sample data for screens that otherwise depend on the internet.",
    ),
    footer1: "16",
  },
  "The current product is Android Native, so the local fallback is an APK/emulator build—not a localhost website. If the rubric strictly requires a localhost URL, prepare and verify a separate local web/backend surface before defense.",
  [
    `${REPO}/app`,
    `${REPO}/gradlew.bat`,
    `${REPO}/app/src/main/java/com/example/madfinalproject`,
  ],
);

addSlide(
  buildSlide26,
  {
    title: "ABROADIQ · READY",
    title2: "One journey.\nEvidence at every step.",
    title3: {
      loremIpsumDetails: "Jira · real ownership",
      loremIpsumDetails2: "GitHub · every member commits",
      loremIpsumDetails3: "Product · cloud + local fallback",
    },
  },
  "Close by restating the value: one connected journey from discovery to preparation and application readiness. Then move directly into the live demonstrations.",
  [PROPOSAL, "https://github.com/MZoHaiB142/MadFinalProject"],
);

for (const [index, slide] of deck.slides.items.entries()) {
  const stem = `slide-${String(index + 1).padStart(2, "0")}`;
  const png = await deck.export({ slide, format: "png", scale: 1 });
  await fs.writeFile(path.join(OUTPUT_DIR, `${stem}.png`), new Uint8Array(await png.arrayBuffer()));
  const layout = await slide.export({ format: "layout" });
  await fs.writeFile(path.join(OUTPUT_DIR, `${stem}.layout.json`), await layout.text());
}

const montage = await deck.export({ format: "webp", montage: true, scale: 0.55 });
await fs.writeFile(
  path.join(OUTPUT_DIR, "AbroadIQ_Project_Presentation_montage.webp"),
  new Uint8Array(await montage.arrayBuffer()),
);

const pptx = await PresentationFile.exportPptx(deck);
await pptx.save(FINAL_PPTX);

console.log(JSON.stringify({
  slideCount: deck.slides.items.length,
  finalPptx: FINAL_PPTX,
  montage: path.join(OUTPUT_DIR, "AbroadIQ_Project_Presentation_montage.webp"),
}, null, 2));
