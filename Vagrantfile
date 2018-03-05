Vagrant.configure("2") do |config|

  config.vm.box = "ubuntu/trusty64"
  config.vm.provider "virtualbox" do |v|
    v.linked_clone = true if Gem::Version.new(Vagrant::VERSION) >= Gem::Version.new('1.8.0')
    # v.memory = 512
    # v.cpus = 2
  end

  instances = %w(seed producer consumer consumer)

  instances.each_with_index do |role, machine_id|
    config.vm.define "akka#{machine_id}-#{role}" do |v|
      v.vm.hostname = "akka#{machine_id}-#{role}"
      v.vm.network "private_network", ip: "192.168.88.#{10 + machine_id}"
      v.vm.provision "ansible" do |ansible|
        ansible.playbook = "ansible/site.yml"
        ansible.extra_vars = {
          akka_role: role
        }
      end
    end
  end

end
