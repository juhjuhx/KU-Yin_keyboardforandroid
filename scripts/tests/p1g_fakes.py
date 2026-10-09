"""Deterministic Android shell double. Never invokes an installed adb."""
import contextlib
import json
from pathlib import Path
import subprocess
import tempfile
from unittest.mock import patch

from test_p1_acceptance_device import device, fake_package


class FakeAndroid:
    def __init__(self, user=0):
        self.user = user
        self.calls = []
        self.mutations = []
        self.after_mutation = None
        self.subtype_count = 0
        self.list_override = None
        self.unsupported = None
        self.states = {user: ["original.ime/.Service\n", "original.ime/.Service\n", "-1\n"]}
        self.properties = {"ro.product.model": "Synthetic", "ro.product.manufacturer": "Fixture",
                           "ro.build.version.sdk": "36", "ro.product.cpu.abilist": "arm64-v8a",
                           "ro.product.cpu.abi": "arm64-v8a", "ro.build.fingerprint": "fake/fingerprint",
                           "ro.kernel.qemu": "0"}

    def __call__(self, argv, **kwargs):
        assert argv[:3] == ["adb", "-s", "FAKE"]
        self.calls.append(argv)
        args = argv[3:]
        output = ""
        if args == ["get-state"]:
            output = "device\n"
        elif args[:2] == ["shell", "getprop"]:
            output = self.properties[args[2]] + "\n"
        elif args == ["shell", "am", "get-current-user"]:
            output = str(self.user) + "\n"
        elif args == ["shell", "settings", "help"]:
            output = "Settings provider (settings) commands:\n  get [--user <USER_ID> | current] NAMESPACE KEY\n"
        elif args == ["shell", "am", "help"]:
            output = "  instrument [-r] [-e <NAME> <VALUE>] [-p <FILE>] [-w]\n          [--user <USER_ID> | current]\n          COMPONENT\n"
        elif args == ["shell", "pm", "help"]:
            output = "  install [-rtfdg] [-i PACKAGE] [--user USER_ID|all|current]\n          [--dont-kill] PATH\n"
        elif args == ["shell", "ime", "help"]:
            output = "ime <command>:\n  enable [--user <USER_ID>] <ID>\n  disable [--user <USER_ID>] <ID>\n  set [--user <USER_ID>] <ID>\n  list [-a] [-s]\n"
        else:
            target = int(args[args.index("--user") + 1]) if "--user" in args else self.user
            state = self.states.setdefault(target, ["original.ime/.Service\n", "original.ime/.Service\n", "-1\n"])
            if args[:2] == ["shell", "settings"]:
                keys = {"default_input_method": 0, "enabled_input_methods": 1, "selected_input_method_subtype": 2}
                output = state[keys[args[-1]]]
            elif args[:3] == ["shell", "ime", "list"]:
                if "-s" in args:
                    output = "original.ime/.Service\n" + device.IME + "\n"
                else:
                    output = self.list_override or ("original.ime/.Service:\n  mId=original.ime/.Service mSettingsActivityName=null\n"
                                                   f"  InputMethodSubtype array: count={self.subtype_count}\n")
            elif args[0] == "install":
                self.mutations.append(argv)
                output = "Success\n"
            elif args[:2] == ["shell", "ime"]:
                self.mutations.append(argv)
                command, component = args[2], args[-1]
                if command == "enable" and component not in state[1]:
                    state[1] = state[1].strip() + ":" + component + "\n"
                elif command == "set":
                    state[0], state[2] = component + "\n", "-1\n"
                elif command == "disable":
                    state[1] = ":".join(x for x in state[1].strip().split(":") if x.split(";")[0] != component) + "\n"
                output = "Input method operation successful\n"
            elif args[:3] == ["shell", "am", "instrument"]:
                self.mutations.append(argv)
                output = "INSTRUMENTATION_RESULT: shortMsg=synthetic failure\nINSTRUMENTATION_CODE: 0\n"
            else:
                raise AssertionError(f"Unknown fake command: {argv!r}")
        if self.unsupported and args[-1:] == ["help"] and args[1] == self.unsupported:
            output = output.replace("--user", "--unsupported")
        if self.after_mutation and argv in self.mutations:
            self.after_mutation(argv)
        return subprocess.CompletedProcess(argv, 0, output, "")


class Session:
    def __init__(self, fake=None):
        self.fake = fake or FakeAndroid()
        self.stack = contextlib.ExitStack()
        self.directory = Path(self.stack.enter_context(tempfile.TemporaryDirectory()))
        self.package, pins = fake_package(self.directory)
        self.evidence = self.directory / "evidence"
        self.stack.enter_context(patch.object(device, "FROZEN", pins))
        real = device.Adb
        self.stack.enter_context(patch.object(device, "Adb", side_effect=lambda serial, log, timeout:
                                             real(serial, log, timeout, executor=self.fake)))

    def __enter__(self):
        return self

    def __exit__(self, *args):
        self.stack.close()

    def execute(self, command):
        argv = ["--serial", "FAKE", "--package-dir", str(self.package), "--evidence-dir", str(self.evidence), command]
        if command == "prepare":
            argv += ["--device-kind", "PHYSICAL"]
        return device.execute(device.parser().parse_args(argv))

    @property
    def state(self):
        return json.loads((self.evidence / device.STATE_NAME).read_text())
