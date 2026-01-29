import os

common_path = r'f:\modmc\bc\mgmc-neoforge-1.21.11\common\src\main\java'

common_java_files = []
for root, dirs, files in os.walk(common_path):
    for file in files:
        if file.endswith('.java'):
            common_java_files.append(os.path.join(root, file))

replacements = {
    "net.minecraft.resources.Identifier": "net.minecraft.resources.ResourceLocation",
    "import javax.annotation.Nullable;": "import org.jetbrains.annotations.Nullable;",
    "import net.minecraft.client.input.CharacterEvent;": "import ltd.opens.mg.mc.client.input.CharacterEvent;",
    "import net.minecraft.client.input.KeyEvent;": "import ltd.opens.mg.mc.client.input.KeyEvent;",
    "import net.minecraft.client.input.MouseButtonEvent;": "import ltd.opens.mg.mc.client.input.MouseButtonEvent;"
}

for file_path in common_java_files:
    if os.path.exists(file_path):
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        new_content = content
        for old, new in replacements.items():
            new_content = new_content.replace(old, new)
        
        if new_content != content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(new_content)
                print(f"Updated {file_path}")
