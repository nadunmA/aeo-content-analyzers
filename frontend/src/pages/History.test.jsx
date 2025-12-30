import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import History from "./History"; // ගැලපෙන path එක දෙන්න

// Global Mocks setup (Fetch & Confirm)
globalThis.fetch = vi.fn();
globalThis.confirm = vi.fn();
globalThis.alert = vi.fn();

const mockData = {
  content: [
    {
      id: "1",
      type: "url",
      title: "Test Report 1",
      createdAt: "2023-01-01T10:00:00Z",
      score: { total: 85, readability: 90, structure: 80, schema: 85 },
    },
    {
      id: "2",
      type: "text",
      title: "Test Report 2",
      createdAt: "2023-01-02T10:00:00Z",
      score: { total: 45, readability: 40, structure: 50, schema: 45 },
    },
  ],
  totalPages: 1,
  totalElements: 2,
};

describe("History Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders loading state initially", () => {
    // Simulate loading state by delaying the promise
    fetch.mockImplementation(() => new Promise(() => {}));
    render(<History onView={vi.fn()} onBack={vi.fn()} />);
    expect(screen.getByText(/Loading audit history.../i)).toBeInTheDocument();
  });

  it("renders data correctly after fetch", async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockData,
    });

    render(<History onView={vi.fn()} onBack={vi.fn()} />);

    // Wait for data to load
    await waitFor(() => {
      expect(screen.getByText("Test Report 1")).toBeInTheDocument();
    });

    // Check stats
    expect(screen.getByText("Test Report 2")).toBeInTheDocument();
    expect(screen.getByText("Excellent")).toBeInTheDocument(); // Score > 80
    expect(screen.getByText("Needs Work")).toBeInTheDocument(); // Score < 50
  });

  it("handles delete action correctly", async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockData,
    });

    render(<History onView={vi.fn()} onBack={vi.fn()} />);

    await waitFor(() => screen.getByText("Test Report 1"));

    // Mock successful delete
    fetch.mockResolvedValueOnce({ ok: true });
    globalThis.confirm.mockReturnValue(true);

    // Find delete button (the XCircle icon wrapper)
    const deleteButtons = screen.getAllByLabelText("Delete Report");
    fireEvent.click(deleteButtons[0]);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(expect.stringContaining("/report/1"), {
        method: "DELETE",
      });
    });
  });

  it("handles error state", async () => {
    fetch.mockRejectedValueOnce(new Error("API Down"));
    render(<History onView={vi.fn()} onBack={vi.fn()} />);

    await waitFor(() => {
      expect(screen.getByText(/Failed to load history/i)).toBeInTheDocument();
    });
  });
});
