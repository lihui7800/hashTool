using System;
using System.Drawing;
using System.IO;
using System.Security.Cryptography;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace SHA256Tool
{
    internal static class Program
    {
        [STAThread]
        private static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new MainForm());
        }
    }

    internal sealed class MainForm : Form
    {
        private readonly TextBox _pathField;
        private readonly Button _chooseBtn;
        private readonly Label _resultLabel;
        private readonly Button _calcBtn;
        private readonly TextBox _resultArea;
        private readonly Button _copyBtn;
        private string _selectedPath;

        private const int RowY1 = 15;
        private const int RowY2 = 55;
        private const int RowY3 = 85;
        private const int RowY4 = 160;

        public MainForm()
        {
            Text = "SHA256Tool v1.0.0";
            FormBorderStyle = FormBorderStyle.FixedSingle;
            MaximizeBox = false;
            StartPosition = FormStartPosition.CenterScreen;
            Font = new Font("Microsoft YaHei UI", 9F);

            // Row 1: path field + choose button
            _pathField = new TextBox();
            _pathField.ReadOnly = true;
            _pathField.Left = 15;
            _pathField.Top = RowY1 + 3;
            _pathField.Width = 385;

            _chooseBtn = new Button();
            _chooseBtn.Text = "选择文件";
            _chooseBtn.Left = 410;
            _chooseBtn.Top = RowY1;
            _chooseBtn.Width = 90;
            _chooseBtn.Height = 27;
            _chooseBtn.Click += OnChoose;

            // Row 2: result label + calc button
            _resultLabel = new Label();
            _resultLabel.Text = "计算结果：";
            _resultLabel.Left = 15;
            _resultLabel.Top = RowY2 + 5;
            _resultLabel.AutoSize = true;
            _resultLabel.Visible = false;

            _calcBtn = new Button();
            _calcBtn.Text = "计算 SHA256";
            _calcBtn.Left = 385;
            _calcBtn.Top = RowY2;
            _calcBtn.Width = 115;
            _calcBtn.Height = 27;
            _calcBtn.Enabled = false;
            _calcBtn.Click += OnCalc;

            // Row 3: result area
            _resultArea = new TextBox();
            _resultArea.Multiline = true;
            _resultArea.ReadOnly = true;
            _resultArea.Left = 15;
            _resultArea.Top = RowY3;
            _resultArea.Width = 490;
            _resultArea.Height = 65;
            _resultArea.WordWrap = true;
            _resultArea.Visible = false;

            // Row 4: copy button
            _copyBtn = new Button();
            _copyBtn.Text = "复制结果";
            _copyBtn.Left = 415;
            _copyBtn.Top = RowY4;
            _copyBtn.Width = 90;
            _copyBtn.Height = 27;
            _copyBtn.Visible = false;
            _copyBtn.Click += OnCopy;

            Controls.Add(_pathField);
            Controls.Add(_chooseBtn);
            Controls.Add(_resultLabel);
            Controls.Add(_calcBtn);
            Controls.Add(_resultArea);
            Controls.Add(_copyBtn);

            SetResultVisible(false);
        }

        private void OnChoose(object sender, EventArgs e)
        {
            using (OpenFileDialog ofd = new OpenFileDialog())
            {
                ofd.Title = "选择文件";
                if (ofd.ShowDialog(this) == DialogResult.OK)
                {
                    _selectedPath = ofd.FileName;
                    _pathField.Text = _selectedPath;
                    _calcBtn.Enabled = true;
                    SetResultVisible(false);
                }
            }
        }

        private async void OnCalc(object sender, EventArgs e)
        {
            if (string.IsNullOrEmpty(_selectedPath)) return;

            _calcBtn.Enabled = false;
            _chooseBtn.Enabled = false;
            Cursor = Cursors.WaitCursor;
            try
            {
                string hash = await Task.Run(() => ComputeSha256(_selectedPath));
                _resultArea.Text = hash;
                SetResultVisible(true);
            }
            catch (Exception ex)
            {
                _resultArea.Text = "计算失败：" + ex.Message;
                SetResultVisible(true);
            }
            finally
            {
                Cursor = Cursors.Default;
                _calcBtn.Enabled = true;
                _chooseBtn.Enabled = true;
            }
        }

        private static string ComputeSha256(string path)
        {
            using (SHA256 sha = SHA256.Create())
            using (FileStream fs = File.OpenRead(path))
            {
                byte[] hash = sha.ComputeHash(fs);
                return BitConverter.ToString(hash).Replace("-", "").ToLowerInvariant();
            }
        }

        private void OnCopy(object sender, EventArgs e)
        {
            if (!string.IsNullOrEmpty(_resultArea.Text))
            {
                try { Clipboard.SetText(_resultArea.Text); }
                catch { /* clipboard busy, ignore */ }
            }
        }

        private void SetResultVisible(bool visible)
        {
            _resultLabel.Visible = visible;
            _resultArea.Visible = visible;
            _copyBtn.Visible = visible;

            if (visible)
            {
                ClientSize = new Size(520, RowY4 + 45);
            }
            else
            {
                ClientSize = new Size(520, RowY2 + 50);
            }
        }
    }
}
