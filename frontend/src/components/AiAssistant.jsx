import { useState, useRef, useEffect } from 'react';
import { sendChatMessage } from '../api';

const SUGGESTIONS = [
  "Where can I eat?",
  "How do I get to Gate B?",
  "When is the next match?",
  "Where are the restrooms?",
  "How to get here by train?",
  "Where can I recycle?",
  "Wheelchair accessibility?",
  "WiFi & charging?",
];

export default function AiAssistant() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([
    {
      role: 'ai',
      text: "👋 Hi! I'm your Smart Stadium AI Assistant. Ask me about navigation, food, matches, transport, accessibility, or anything about the stadium!",
    },
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const endRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    if (endRef.current) endRef.current.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  useEffect(() => {
    if (open && inputRef.current) inputRef.current.focus();
  }, [open]);

  const send = async (text) => {
    const msg = text || input.trim();
    if (!msg || loading) return;
    setInput('');
    setMessages(prev => [...prev, { role: 'user', text: msg }]);
    setLoading(true);

    try {
      const data = await sendChatMessage(msg);
      setMessages(prev => [...prev, { role: 'ai', text: data.response }]);
    } catch {
      setMessages(prev => [...prev, { role: 'ai', text: "Sorry, I'm having trouble connecting. Please try again!" }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      {/* FAB */}
      <button className={`ai-fab${open ? ' hidden' : ''}`} onClick={() => setOpen(true)} title="AI Assistant">
        <span className="ai-fab-icon">🤖</span>
        <span className="ai-fab-pulse" />
      </button>

      {/* Chat Panel */}
      {open && (
        <div className="ai-panel">
          <div className="ai-header">
            <div className="ai-header-info">
              <span className="ai-header-icon">🤖</span>
              <div>
                <span className="ai-header-title">Stadium AI Assistant</span>
                <span className="ai-header-status">● Online</span>
              </div>
            </div>
            <button className="ai-close" onClick={() => setOpen(false)}>✕</button>
          </div>

          <div className="ai-messages">
            {messages.map((m, i) => (
              <div key={i} className={`ai-msg ${m.role}`}>
                {m.role === 'ai' && <span className="ai-msg-avatar">🤖</span>}
                <div className="ai-msg-bubble" dangerouslySetInnerHTML={{
                  __html: m.text
                    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
                    .replace(/\n/g, '<br/>')
                }} />
              </div>
            ))}
            {loading && (
              <div className="ai-msg ai">
                <span className="ai-msg-avatar">🤖</span>
                <div className="ai-msg-bubble ai-typing">
                  <span className="dot" /><span className="dot" /><span className="dot" />
                </div>
              </div>
            )}
            <div ref={endRef} />
          </div>

          {/* Suggestions */}
          {messages.length <= 2 && (
            <div className="ai-suggestions">
              {SUGGESTIONS.slice(0, 4).map((s, i) => (
                <button key={i} className="ai-suggestion" onClick={() => send(s)}>{s}</button>
              ))}
            </div>
          )}

          <div className="ai-input-row">
            <input
              ref={inputRef}
              className="ai-input"
              placeholder="Ask about the stadium..."
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && send()}
              disabled={loading}
            />
            <button className="ai-send" onClick={() => send()} disabled={!input.trim() || loading}>
              ➤
            </button>
          </div>
        </div>
      )}
    </>
  );
}
