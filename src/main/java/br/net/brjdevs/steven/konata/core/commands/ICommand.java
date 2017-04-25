package br.net.brjdevs.steven.konata.core.commands;

import net.dv8tion.jda.core.Permission;

import java.util.function.Consumer;

public interface ICommand {
    void invoke(CommandEvent event);

    String getName();

    String getDescription();

    String getUsageInstruction();

    String[] getAliases();

    Category getCategory();

    boolean isPrivateAvailable();

    boolean isOwnerOnly();

    Permission[] getRequiredPermission();

    class Builder {
        private Consumer<CommandEvent> action;
        private String name, description, usage;
        private String[] aliases;
        Category category;
        private boolean privateAvailable, ownerOnly;
        Permission[] requiredPermission;

        public Builder setAction(Consumer<CommandEvent> action) {
            this.action = action;
            return this;
        }
        public Builder setAliases(String... aliases) {
            this.aliases = aliases;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setOwnerOnly(boolean ownerOnly) {
            this.ownerOnly = ownerOnly;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setPrivateAvailable(boolean privateAvailable) {
            this.privateAvailable = privateAvailable;
            return this;
        }

        public Builder setRequiredPermission(Permission... requiredPermission) {
            this.requiredPermission = requiredPermission;
            return this;
        }

        public Builder setUsageInstruction(String usage) {
            this.usage = usage;
            return this;
        }

        public Builder setCategory(Category category) {
            this.category = category;
            return this;
        }
        public ICommand build() {
            return new ICommand() {
                @Override
                public void invoke(CommandEvent event) {
                    action.accept(event);
                }
                @Override
                public String getName() {
                    return name;
                }
                @Override
                public String getDescription() {
                    return description;
                }
                @Override
                public String getUsageInstruction() {
                    return usage;
                }
                @Override
                public String[] getAliases() {
                    return aliases;
                }
                @Override
                public Category getCategory() {
                    return category;
                }
                @Override
                public boolean isPrivateAvailable() {
                    return privateAvailable;
                }
                @Override
                public boolean isOwnerOnly() {
                    return ownerOnly;
                }
                @Override
                public Permission[] getRequiredPermission() {
                    return requiredPermission;
                }
            };
        }
    }
}
